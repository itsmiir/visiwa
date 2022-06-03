/*
 * Copyright (c) 2022-2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.world.biome.source;

import com.miir.visiwa.Visiwa;
import com.miir.visiwa.world.gen.Atlas;
import com.miir.visiwa.world.gen.AtlasHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryCodecs;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.List;
import java.util.Set;

public class VisiwaBiomeSource extends BiomeSource {
    public static final Codec<VisiwaBiomeSource> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(
                    RegistryCodecs.dynamicRegistry(Registry.BIOME_KEY, Lifecycle.stable(), Biome.CODEC)
                                    .fieldOf("biome_registry")
                                    .stable()
                                    .forGetter(VisiwaBiomeSource::getBiomeRegistry),
                            Codec.LONG.fieldOf("seed")
                                            .stable()
                                                    .forGetter(VisiwaBiomeSource::getSeed),
                            Atlas.CODEC.fieldOf("atlas")
                                    .stable()
                                    .forGetter(VisiwaBiomeSource::getAtlas))
                    .apply(instance, instance.stable(VisiwaBiomeSource::new)));
    private final Atlas atlas;
    private final long seed;
    private final Registry<Biome> biomes;

    public VisiwaBiomeSource(Registry<Biome> biomes, long seed, Atlas atlas) {
        super(biomes.streamEntries().map(biome -> biomes.entryOf(biome.registryKey())));
        this.atlas = atlas;
        this.seed = seed;
        this.biomes = biomes;
    }
    public VisiwaBiomeSource(Registry<Biome> biomes, Atlas atlas) {
        this(biomes, atlas.getSeed(), atlas);
    }

    public long getSeed() {
        return this.seed;
    }

    public Registry<Biome> getBiomeRegistry() {
        return this.biomes;
    }

    public Atlas getAtlas() {
        return this.atlas;
    }

    @Override
    public void addDebugInfo(List<String> info, BlockPos pos, MultiNoiseUtil.MultiNoiseSampler noiseSampler) {
        int i = BiomeCoords.fromBlock(pos.getX());
        int k = BiomeCoords.fromBlock(pos.getZ());
        info.add("Regional elevation: " + Visiwa.atlas.getY(i, k));
        info.add("Regional temperature: " + Visiwa.atlas.getT(i, k));
        info.add("Regional humidity: " + Visiwa.atlas.getH(i, k));
    }

    @Override
    protected Codec<? extends BiomeSource> getCodec() {
        return CODEC;
    }

    @Override
    public RegistryEntry<Biome> getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise) {
        if (!atlas.isBuilt()) {
            atlas.draw();
        }
        if (!Visiwa.atlas.isLand(AtlasHelper.clean(x/16), AtlasHelper.clean(z/16))) {
            return BuiltinRegistries.BIOME.entryOf(BiomeKeys.OCEAN);
        } else {
            return BuiltinBiomes.getDefaultBiome(this.biomes);
        }
    }

    @Override
    public Set<RegistryEntry<Biome>> getBiomes() {
        return super.getBiomes();
    }
}
