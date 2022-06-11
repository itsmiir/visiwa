/*
 * Copyright (c) 2022-2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.world.gen.chunk;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.miir.visiwa.mixin.ChunkNoiseSamplerAccessor;
import com.miir.visiwa.world.biome.source.VisiwaBiomeSource;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryCodecs;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureWeightSampler;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

public class VisiwaChunkGenerator extends NoiseChunkGenerator {
    private VisiwaBiomeSource source;
    private Registry<StructureSet> structures;
    private RegistryEntry<ChunkGeneratorSettings> settings;
    private BlockState defaultBlock = Blocks.STONE.getDefaultState();
    private NoiseRouter noiseRouter;
    private Registry<DoublePerlinNoiseSampler.NoiseParameters> perlinRegistry;
    private AquiferSampler.FluidLevelSampler fluidLevelSampler;


    public static final Codec<VisiwaChunkGenerator> CODEC = RecordCodecBuilder.create(
            in -> in.group(
                            RegistryCodecs.dynamicRegistry(Registry.STRUCTURE_SET_KEY, Lifecycle.stable(), StructureSet.CODEC)
                                    .fieldOf("structures")
                                    .stable()
                                    .forGetter(VisiwaChunkGenerator::getStructures),
                            ((Codec<VisiwaBiomeSource>) (Object) VisiwaBiomeSource.CODEC)
                                    .fieldOf("source")
                                    .stable()
                                    .forGetter(VisiwaChunkGenerator::getSource),
                            ChunkGeneratorSettings.REGISTRY_CODEC
                                    .fieldOf("settings")
                                    .stable()
                                    .forGetter(VisiwaChunkGenerator::getSettings),
                            RegistryOps.createRegistryCodec(Registry.NOISE_KEY)
                                    .fieldOf("perlin")
                                    .stable()
                                    .forGetter(VisiwaChunkGenerator::getPerlinRegistry)
                    )
                    .apply(in, VisiwaChunkGenerator::new)
    );

    public VisiwaChunkGenerator(Registry<StructureSet> structureSets, VisiwaBiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistryEntry, Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseParameters) {
        super(structureSets, noiseParameters, biomeSource, chunkGeneratorSettingsRegistryEntry);
    }

    public Registry<DoublePerlinNoiseSampler.NoiseParameters> getPerlinRegistry() {
        return this.perlinRegistry;
    }

    public RegistryEntry<ChunkGeneratorSettings> getSettings() {
        return this.settings;
    }

    public VisiwaBiomeSource getSource() {
        return this.source;
    }

    public Registry<StructureSet> getStructures() {
        return this.structures;
    }

@Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess world, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {

    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
        if (SharedConstants.method_37896(chunk.getPos())) {
            return;
        }
        HeightContext heightContext = new HeightContext(this, region);
        this.buildSurface(chunk, heightContext, noiseConfig, structures, region.getBiomeAccess(), region.getRegistryManager().get(Registry.BIOME_KEY), Blender.getBlender(region));
    }

    @VisibleForTesting
    public void buildSurface(Chunk chunk2, HeightContext heightContext, NoiseConfig noiseConfig, StructureAccessor structureAccessor, BiomeAccess biomeAccess, Registry<Biome> registry, Blender blender) {
        ChunkNoiseSampler chunkNoiseSampler = chunk2.getOrCreateChunkNoiseSampler(chunk -> this.buildChunkNoiseSampler(chunk, structureAccessor, blender, noiseConfig));
        ChunkGeneratorSettings chunkGeneratorSettings = this.settings.value();
        noiseConfig.getSurfaceBuilder().buildSurface(noiseConfig, biomeAccess, registry, chunkGeneratorSettings.usesLegacyRandom(), heightContext, chunk2, chunkNoiseSampler, chunkGeneratorSettings.surfaceRule());
    }
    private ChunkNoiseSampler buildChunkNoiseSampler(Chunk chunk, StructureAccessor structureAccessor, Blender blender, NoiseConfig noiseConfig) {
        return ChunkNoiseSampler.create(chunk, noiseConfig, StructureWeightSampler.method_42695(structureAccessor, chunk.getPos()), this.settings.value(), this.fluidLevelSampler, blender);
    }

    @Override
    public void populateEntities(ChunkRegion region) {
        if (this.settings.value().mobGenerationDisabled()) {
            return;
        }
        ChunkPos chunkPos = region.getCenterPos();
        RegistryEntry<Biome> registryEntry = region.getBiome(chunkPos.getStartPos().withY(region.getTopY() - 1));
        ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(RandomSeed.getSeed()));
        chunkRandom.setPopulationSeed(region.getSeed(), chunkPos.getStartX(), chunkPos.getStartZ());
        SpawnHelper.populateEntities(region, registryEntry, chunkPos, chunkRandom);
    }

    @Override
    public int getWorldHeight() {
        return this.settings.value().generationShapeConfig().height();
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        GenerationShapeConfig generationShapeConfig = this.settings.value().generationShapeConfig().method_42368(chunk.getHeightLimitView());
        int i = generationShapeConfig.minimumY();
        int j = MathHelper.floorDiv(i, generationShapeConfig.verticalBlockSize());
        int k = MathHelper.floorDiv(generationShapeConfig.height(), generationShapeConfig.verticalBlockSize());
        if (k <= 0) {
            return CompletableFuture.completedFuture(chunk);
        }
        int l = chunk.getSectionIndex(k * generationShapeConfig.verticalBlockSize() - 1 + i);
        int m = chunk.getSectionIndex(i);
        HashSet<ChunkSection> set = Sets.newHashSet();
        for (int n = l; n >= m; --n) {
            ChunkSection chunkSection = chunk.getSection(n);
            chunkSection.lock();
            set.add(chunkSection);
        }
        return CompletableFuture.supplyAsync(Util.debugSupplier("wgen_fill_noise", () -> this.populateNoise(blender, structureAccessor, noiseConfig, chunk, j, k)), Util.getMainWorkerExecutor()).whenCompleteAsync((c, throwable) -> {
            for (ChunkSection chunkSection : set) {
                chunkSection.unlock();
            }
        }, executor);
    }

    private ChunkNoiseSampler create(Chunk chunk, StructureAccessor structureAccessor, Blender blender, NoiseConfig noiseConfig) {
        return ChunkNoiseSampler.create(chunk, noiseConfig, StructureWeightSampler.method_42695(structureAccessor, chunk.getPos()), this.settings.value(), this.fluidLevelSampler, blender);
    }

    private Chunk populateNoise(Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk2, int i, int j) {
        ChunkNoiseSampler chunkNoiseSampler = chunk2.getOrCreateChunkNoiseSampler(chunk -> this.create(chunk, structureAccessor, blender, noiseConfig));
        Heightmap heightmap = chunk2.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunk2.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunk2.getPos();
        int k = chunkPos.getStartX();
        int l = chunkPos.getStartZ();
        AquiferSampler aquiferSampler = chunkNoiseSampler.getAquiferSampler();
        chunkNoiseSampler.sampleStartNoise();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int m = ((ChunkNoiseSamplerAccessor) chunkNoiseSampler).callMethod_42361();
        int n = ((ChunkNoiseSamplerAccessor) chunkNoiseSampler).callMethod_42362();
        int o = 16 / m;
        int p = 16 / m;
        for (int q = 0; q < o; ++q) {
            chunkNoiseSampler.sampleEndNoise(q);
            for (int r = 0; r < p; ++r) {
                ChunkSection chunkSection = chunk2.getSection(chunk2.countVerticalSections() - 1);
                for (int s = j - 1; s >= 0; --s) {
                    chunkNoiseSampler.sampleNoiseCorners(s, r);
                    for (int t = n - 1; t >= 0; --t) {
                        int u = (i + s) * n + t;
                        int v = u & 0xF;
                        int w = chunk2.getSectionIndex(u);
                        if (chunk2.getSectionIndex(chunkSection.getYOffset()) != w) {
                            chunkSection = chunk2.getSection(w);
                        }
                        double d = (double)t / (double)n;
                        chunkNoiseSampler.sampleNoiseY(u, d);
                        for (int x = 0; x < m; ++x) {
                            int y = k + q * m + x;
                            int z = y & 0xF;
                            double e = (double)x / (double)m;
                            chunkNoiseSampler.sampleNoiseX(y, e);
                            for (int aa = 0; aa < m; ++aa) {
                                int ab = l + r * m + aa;
                                int ac = ab & 0xF;
                                double f = (double)aa / (double)m;
                                chunkNoiseSampler.sampleNoise(ab, f);
                                BlockState blockState = ((ChunkNoiseSamplerAccessor)chunkNoiseSampler).callSampleBlockState();
                                if (blockState == null) {
                                    blockState = this.defaultBlock;
                                }
                                if (blockState == Blocks.AIR.getDefaultState() || SharedConstants.method_37896(chunk2.getPos())) continue;
                                if (blockState.getLuminance() != 0 && chunk2 instanceof ProtoChunk) {
                                    mutable.set(y, u, ab);
                                    ((ProtoChunk)chunk2).addLightSource(mutable);
                                }
                                chunkSection.setBlockState(z, v, ac, blockState, false);
                                heightmap.trackUpdate(z, u, ac, blockState);
                                heightmap2.trackUpdate(z, u, ac, blockState);
                                if (!aquiferSampler.needsFluidTick() || blockState.getFluidState().isEmpty()) continue;
                                mutable.set(y, u, ab);
                                chunk2.markBlockForPostProcessing(mutable);
                            }
                        }
                    }
                }
            }
            chunkNoiseSampler.swapBuffers();
        }
        chunkNoiseSampler.method_40537();
        return chunk2;
    }
    private BlockState getBlockState(ChunkNoiseSampler chunkNoiseSampler, int x, int y, int z, BlockState state) {
        return state;
    }

    @Override
    public int getSeaLevel() {
        return this.settings.value().seaLevel();
    }

    @Override
    public int getMinimumY() {
        return this.settings.value().generationShapeConfig().minimumY();
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        GenerationShapeConfig generationShapeConfig = this.settings.value().generationShapeConfig();
        int i = Math.max(generationShapeConfig.minimumY(), world.getBottomY());
        int j = Math.min(generationShapeConfig.minimumY() + generationShapeConfig.height(), world.getTopY());
        int k = MathHelper.floorDiv(i, generationShapeConfig.verticalBlockSize());
        int l = MathHelper.floorDiv(j - i, generationShapeConfig.verticalBlockSize());
        if (l <= 0) {
            return world.getBottomY();
        }

        return 128; // todo: basically everything in this class is a todo
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        MutableObject<VerticalBlockSample> mutableObject = new MutableObject<VerticalBlockSample>();
        this.sampleHeightmap(world, noiseConfig, x, z, mutableObject, null);
        return mutableObject.getValue();
    }

    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
//    todo: something should go here eventually
    }

    private OptionalInt sampleHeightmap(HeightLimitView heightLimitView, NoiseConfig noiseConfig, int i, int j, @Nullable MutableObject<VerticalBlockSample> mutableObject, @Nullable Predicate<BlockState> predicate) {
        BlockState[] blockStates;
        GenerationShapeConfig generationShapeConfig = this.settings.value().generationShapeConfig().method_42368(heightLimitView);
        int k = generationShapeConfig.verticalBlockSize();
        int l = generationShapeConfig.minimumY();
        int m = MathHelper.floorDiv(l, k);
        int n = MathHelper.floorDiv(generationShapeConfig.height(), k);
        if (n <= 0) {
            return OptionalInt.empty();
        }
        if (mutableObject == null) {
            blockStates = null;
        } else {
            blockStates = new BlockState[generationShapeConfig.height()];
            mutableObject.setValue(new VerticalBlockSample(l, blockStates));
        }
        int o = generationShapeConfig.horizontalBlockSize();
        int p = Math.floorDiv(i, o);
        int q = Math.floorDiv(j, o);
        int r = Math.floorMod(i, o);
        int s = Math.floorMod(j, o);
        int t = p * o;
        int u = q * o;
        double d = (double)r / (double)o;
        double e = (double)s / (double)o;
        ChunkNoiseSampler chunkNoiseSampler = new ChunkNoiseSampler(1, noiseConfig, t, u, generationShapeConfig, Beardifier.INSTANCE, this.settings.value(), this.fluidLevelSampler, Blender.getNoBlending());
        chunkNoiseSampler.sampleStartNoise();
        chunkNoiseSampler.sampleEndNoise(0);
        for (int v = n - 1; v >= 0; --v) {
            chunkNoiseSampler.sampleNoiseCorners(v, 0);
            for (int w = k - 1; w >= 0; --w) {
                BlockState blockState2;
                int x = (m + v) * k + w;
                double f = (double)w / (double)k;
                chunkNoiseSampler.sampleNoiseY(x, f);
                chunkNoiseSampler.sampleNoiseX(i, d);
                chunkNoiseSampler.sampleNoise(j, e);
                BlockState blockState = ((ChunkNoiseSamplerAccessor) chunkNoiseSampler).callSampleBlockState();
                BlockState blockState3 = blockState2 = blockState == null ? this.defaultBlock : blockState;
                if (blockStates != null) {
                    int y = v * k + w;
                    blockStates[y] = blockState2;
                }
                if (predicate == null || !predicate.test(blockState2)) continue;
                chunkNoiseSampler.method_40537();
                return OptionalInt.of(x + 1);
            }
        }
        chunkNoiseSampler.method_40537();
        return OptionalInt.empty();
    }
    protected static enum Beardifier implements DensityFunctionTypes.class_7050 {
        INSTANCE;

        private Beardifier() {
        }

        public double sample(NoisePos pos) {
            return 0.0D;
        }

        public void method_40470(double[] ds, class_6911 arg) {
            Arrays.fill(ds, 0.0D);
        }

        public double minValue() {
            return 0.0D;
        }

        public double maxValue() {
            return 0.0D;
        }
    }


}
