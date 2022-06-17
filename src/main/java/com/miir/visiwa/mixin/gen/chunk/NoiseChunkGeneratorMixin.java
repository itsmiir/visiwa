/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.mixin.gen.chunk;

import com.google.common.collect.Sets;
import com.miir.visiwa.Visiwa;
import com.miir.visiwa.world.gen.atlas.AtlasSubSampler;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(NoiseChunkGenerator.class)
public abstract class NoiseChunkGeneratorMixin {
    @Shadow @Final protected BlockState defaultBlock;
    @Shadow protected abstract ChunkNoiseSampler method_41537(Chunk chunk, StructureAccessor structureAccessor, Blender blender, NoiseConfig noiseConfig);
    @Shadow @Final protected RegistryEntry<ChunkGeneratorSettings> settings;
    @Shadow @Final private static BlockState AIR;
    @Shadow public abstract int getSeaLevel();

    /**
     * @author miir
     */
    @Overwrite
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
//        get the GenerationShapeConfig, which contains some noise settings from the datapack
        GenerationShapeConfig generationShapeConfig = this.settings.value().generationShapeConfig().method_42368(chunk.getHeightLimitView());
        int minimumY = generationShapeConfig.minimumY(); // limited to multiples of 16
        int minimumBlockY = MathHelper.floorDiv(minimumY, generationShapeConfig.verticalBlockSize()); // for surface, 8
        int worldBlockHeight = MathHelper.floorDiv(generationShapeConfig.height(), generationShapeConfig.verticalBlockSize()); // verticalSize stretches the landmass (higher value == higher land)
        // if height is 0 nothing should generate
        if (worldBlockHeight <= 0) {
            return CompletableFuture.completedFuture(chunk);
        }

        // lock each chunk section (presumably cannot be edited elsewhere?)
        int maxSectionIndex = chunk.getSectionIndex(worldBlockHeight * generationShapeConfig.verticalBlockSize() - 1 + minimumY);
        int minSectionIndex = chunk.getSectionIndex(minimumY);
        HashSet<ChunkSection> chunkSections = Sets.newHashSet();
        for (int n = maxSectionIndex; n >= minSectionIndex; --n) {
            ChunkSection chunkSection = chunk.getSection(n);
            chunkSection.lock();
            chunkSections.add(chunkSection);
        }

//        send the chunk to a worker thread for generation
        return CompletableFuture.supplyAsync(Util.debugSupplier("wgen_fill_noise", () -> this.populateNoise(blender, structureAccessor, noiseConfig, chunk, minimumBlockY, worldBlockHeight)), Util.getMainWorkerExecutor()).whenCompleteAsync((c, throwable) -> {
            for (ChunkSection chunkSection : chunkSections) {
                chunkSection.unlock();
            }
        }, executor);
    }


    private Chunk populateNoise(Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minCubeY, int worldHeight) {
//        get the noise sampler
        ChunkNoiseSampler chunkNoiseSampler = chunk.getOrCreateChunkNoiseSampler(chunk1 -> this.method_41537(chunk1, structureAccessor, blender, noiseConfig));

        int seaLevel = this.getSeaLevel();
        BlockState defaultFluid = Blocks.WATER.getDefaultState();
//        get the chunk heightmaps for the land and sea, so we can update them when we place each block
        Heightmap oceanFloorHeightmap = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap worldSurfaceHeightmap = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);


        ChunkPos chunkPos = chunk.getPos();
        int chunkStartX = chunkPos.getStartX();
        int chunkStartZ = chunkPos.getStartZ();
        AquiferSampler aquiferSampler = chunkNoiseSampler.getAquiferSampler();
        chunkNoiseSampler.sampleStartNoise(); // startInterpolation
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        int horizontalBlockSize = chunkNoiseSampler.method_42361(); // horizontal and vertical block size
        int verticalBlockSize = chunkNoiseSampler.method_42362();
        int horizontalX = 16 / horizontalBlockSize; // number of sub-chunks in the chunk (16 = the number of blocks across a chunk)
        int horizontalZ = 16 / horizontalBlockSize;

//        iterate through each 4x4 section of the chunk
        for (int chunkCubeX = 0; chunkCubeX < horizontalX; ++chunkCubeX) {
            chunkNoiseSampler.sampleEndNoise(chunkCubeX);
            for (int chunkCubeZ = 0; chunkCubeZ < horizontalZ; ++chunkCubeZ) {
//                get the top section of the chunk
                ChunkSection chunkSection = chunk.getSection(chunk.countVerticalSections() - 1);
                for (int chunkCubeY = worldHeight - 1; chunkCubeY >= 0; --chunkCubeY) {
                    chunkNoiseSampler.sampleNoiseCorners(chunkCubeY, chunkCubeZ);

//                    get local and absolute y coords
                    for (int deltaY = verticalBlockSize - 1; deltaY >= 0; --deltaY) {
                        int absoluteY = (minCubeY + chunkCubeY) * verticalBlockSize + deltaY;
                        int localY = absoluteY & 0xF;
                        int chunkSectionIndex = chunk.getSectionIndex(absoluteY);

//                        ensure that the working chunk section is the same as the absoluteY one
                        if (chunk.getSectionIndex(chunkSection.getYOffset()) != chunkSectionIndex) {
                            chunkSection = chunk.getSection(chunkSectionIndex);
                        }

//                        the percentage of the way through the top of the chunk we are
                        double yLerpAmount = (double)deltaY / (double)verticalBlockSize;
                        chunkNoiseSampler.sampleNoiseY(absoluteY, yLerpAmount);

//                        get local and absolute x coords
                        for (int x = 0; x < horizontalBlockSize; ++x) {
                            int absoluteX = chunkStartX + chunkCubeX * horizontalBlockSize + x;
                            int localX = absoluteX & 0xF;

//                            the percentage of the way through the chunk we are (x direction)
                            double xLerpAmount = (double)x / (double)horizontalBlockSize;
                            chunkNoiseSampler.sampleNoiseX(absoluteX, xLerpAmount);

//                            get local and absolute z coords
                            for (int z = 0; z < horizontalBlockSize; ++z) {
                                int absoluteZ = chunkStartZ + chunkCubeZ * horizontalBlockSize + z;
                                int localZ = absoluteZ & 0xF;

                                double zLerpAmount = (double) z / (double) horizontalBlockSize;
                                chunkNoiseSampler.sampleNoise(absoluteZ, zLerpAmount); // sampleNoise should probably be sampleNoiseZ

//                                sample the blockstate at the current sample point
                                BlockState blockState = AtlasSubSampler.subPixelSample(absoluteX, absoluteY, absoluteZ, seaLevel, defaultFluid);
//                                BlockState blockState = chunkNoiseSampler.subPixelSample();
//                                localY: [0, 15]
//                                deltaY: [0, 7]
//                                absoluteY: [-64, -37] U [99, 319]
                                if (blockState == null) {
                                    blockState = this.defaultBlock;
                                }
//                                if the blockstate is air or the world is debug and the current chunk is outside a certain area, no lighting, water, or heightmap updates needed
                                if (blockState == AIR || SharedConstants.method_37896(chunk.getPos())) continue;

//                                add the light source to the chunk if it's a ProtoChunk (something to do with lighting)
                                if (blockState.getLuminance() != 0 && chunk instanceof ProtoChunk) {
                                    mutablePos.set(absoluteX, absoluteY, absoluteZ);
                                    ((ProtoChunk) chunk).addLightSource(mutablePos);
                                }

//                                set the blockstate to the blockstate we defined
                                chunkSection.setBlockState(localX, localY, localZ, blockState, false);

//                                update the heightmaps
                                oceanFloorHeightmap.trackUpdate(localX, absoluteY, localZ, blockState);
                                worldSurfaceHeightmap.trackUpdate(localX, absoluteY, localZ, blockState);

//                                if there's no fluid at this blockpos or the aquifer placer says it's okay, no post-processing needed
                                if (!aquiferSampler.needsFluidTick() || blockState.getFluidState().isEmpty())
                                    continue;
                                mutablePos.set(absoluteX, absoluteY, absoluteZ);
                                chunk.markBlockForPostProcessing(mutablePos);
                            }
                        }
                    }
                }
            }
//            swap the startNoiseBuffer and the endNoiseBuffer
            chunkNoiseSampler.swapBuffers();
        }
        chunkNoiseSampler.method_40537(); // mark as finished
        return chunk;
    }

    @Inject(at = @At("HEAD"), method = "getHeight", cancellable = true)
    private void mixin(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig, CallbackInfoReturnable<Integer> cir) {
        if (Visiwa.isAtlas) {
            cir.setReturnValue(AtlasSubSampler.getHeight(x, z));
        }
    }
}
