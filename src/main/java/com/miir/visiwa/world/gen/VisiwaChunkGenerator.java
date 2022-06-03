/*
 * Copyright (c) 2022-2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.world.gen;

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

public class VisiwaChunkGenerator extends ChunkGenerator {

    private final long seed;
    private final VisiwaBiomeSource source;
    private final Registry<StructureSet> structures;
    private final RegistryEntry<ChunkGeneratorSettings> settings;
    private final BlockState defaultBlock = Blocks.STONE.getDefaultState();
    private final NoiseRouter noiseRouter;
    private final Registry<DoublePerlinNoiseSampler.NoiseParameters> perlinRegistry;
    private final AquiferSampler.FluidLevelSampler fluidLevelSampler;

    public static final Codec<VisiwaChunkGenerator> CODEC = RecordCodecBuilder.create(
            in -> in.group(
                    RegistryCodecs.dynamicRegistry(Registry.STRUCTURE_SET_KEY, Lifecycle.stable(), StructureSet.CODEC)
                            .fieldOf("structures")
                            .stable()
                            .forGetter(VisiwaChunkGenerator::getStructures),
                    VisiwaBiomeSource.CODEC
                            .fieldOf("source")
                            .stable()
                            .forGetter(VisiwaChunkGenerator::getSource),
                    Codec.LONG
                            .fieldOf("seed")
                            .stable()
                            .forGetter(VisiwaChunkGenerator::getSeed),
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

    public long getSeed() {
        return this.seed;
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

    public VisiwaChunkGenerator(Registry<StructureSet> registry, VisiwaBiomeSource source, long seed, RegistryEntry<ChunkGeneratorSettings> settings, Registry<DoublePerlinNoiseSampler.NoiseParameters> perlinParams) {
        super(registry, Optional.empty(), source);
        this.structures = registry;
        this.settings = settings;
        this.noiseRouter = this.settings.value().noiseRouter();
        this.seed = seed;
        this.source = source;
        this.perlinRegistry = perlinParams;
        int seaLevel = settings.value().seaLevel();
        AquiferSampler.FluidLevel fluidLevel = new AquiferSampler.FluidLevel(-54, Blocks.LAVA.getDefaultState());
        AquiferSampler.FluidLevel fluidLevel2 = new AquiferSampler.FluidLevel(seaLevel, settings.value().defaultFluid());
        this.fluidLevelSampler = (x, y, z) -> {
            if (y < Math.min(-54, seaLevel)) {
                return fluidLevel;
            }
            return fluidLevel2;
        };
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
        GenerationShapeConfig generationShapeConfig = this.settings.value().generationShapeConfig();
        HeightLimitView heightLimitView = chunk.getHeightLimitView();
        int i = Math.max(generationShapeConfig.minimumY(), heightLimitView.getBottomY());
        int j = Math.min(generationShapeConfig.minimumY() + generationShapeConfig.height(), heightLimitView.getTopY());
        int k = MathHelper.floorDiv(i, generationShapeConfig.verticalBlockSize());
        int l = MathHelper.floorDiv(j - i, generationShapeConfig.verticalBlockSize());
        if (l <= 0) {
            return CompletableFuture.completedFuture(chunk);
        }
        int m = chunk.getSectionIndex(l * generationShapeConfig.verticalBlockSize() - 1 + i);
        int n = chunk.getSectionIndex(i);
        HashSet<ChunkSection> set = Sets.newHashSet();
        for (int o = m; o >= n; --o) {
            ChunkSection chunkSection = chunk.getSection(o);
            chunkSection.lock();
            set.add(chunkSection);
        }
        System.out.println("noise population bypassed by chunk generator!");
        return CompletableFuture.completedFuture(chunk); // todo: don't forget to fix this ;)
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
