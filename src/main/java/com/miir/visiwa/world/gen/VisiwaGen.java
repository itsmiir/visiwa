/*
 * Copyright (c) 2022-2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.world.gen;

import com.miir.visiwa.world.biome.source.VisiwaBiomeSource;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.*;

import java.util.Optional;

public class VisiwaGen {
    public static final GeneratorType ATLAS = new GeneratorType("visiwa") {
        @Override
        public ChunkGenerator getChunkGenerator(DynamicRegistryManager registryManager, long seed) {
            Registry<Biome> biomeRegistry = registryManager.get(Registry.BIOME_KEY);
            Registry<StructureSet> structureRegistry = registryManager.get(Registry.STRUCTURE_SET_KEY);
            Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseRegistry = registryManager.get(Registry.NOISE_WORLDGEN);
            Registry<ChunkGeneratorSettings> generatorSettings = registryManager.get(Registry.CHUNK_GENERATOR_SETTINGS_KEY);
//            return new FlatChunkGenerator(structureRegistry, FlatChunkGeneratorConfig.getDefaultConfig(biomeRegistry, structureRegistry));
//            return new NoiseChunkGenerator(structureRegistry, noiseRegistry, new MultiNoiseBiomeSource(MultiNoiseUtil.), seed, generatorSettings.getOrCreateEntry(ChunkGeneratorSettings.OVERWORLD));
            return new NoiseChunkGenerator(structureRegistry, noiseRegistry, new VisiwaBiomeSource(biomeRegistry, seed, new Atlas(seed)), seed, generatorSettings.getOrCreateEntry(ChunkGeneratorSettings.OVERWORLD));
        }
    };

    public static void register() {
    }
}
