/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.mixin.gen;


import com.miir.visiwa.Visiwa;
import com.miir.visiwa.world.biome.source.VisiwaBiomeSource;
import com.miir.visiwa.world.gen.Atlas;
import com.miir.visiwa.world.gen.VisiwaChunkGenerator;
import com.miir.visiwa.world.gen.VisiwaGen;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(WorldPresets.class)
public class InjectPresetMixin {

    @Inject(at = @At("HEAD"), method = "initAndGetDefault")
    private static void mixin(Registry<WorldPreset> registry, CallbackInfoReturnable<RegistryEntry<WorldPreset>> cir) {
        Registry<StructureSet> structureSetRegistry = BuiltinRegistries.STRUCTURE_SET;
        Registry<Biome> biomeRegistry = BuiltinRegistries.BIOME;
        Registry<DimensionType> dimensionTypeRegistry = BuiltinRegistries.DIMENSION_TYPE;
        Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry = BuiltinRegistries.CHUNK_GENERATOR_SETTINGS;

        RegistryEntry<DimensionType> overworldDimensionType = dimensionTypeRegistry.getOrCreateEntry(DimensionTypes.OVERWORLD);
        Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseParametersRegistry = BuiltinRegistries.NOISE_PARAMETERS;
        Atlas atlas = new Atlas(Visiwa.RANDOM.nextLong()); // todo: change this so it's not just a random seed each time
        DimensionOptions dimensionOptions = new DimensionOptions(
                overworldDimensionType,
                new VisiwaChunkGenerator(
                        structureSetRegistry,
                        new VisiwaBiomeSource(biomeRegistry, atlas),
                        atlas.getSeed(),
                        BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.getOrCreateEntry(ChunkGeneratorSettings.OVERWORLD),
                        noiseParametersRegistry));

        RegistryEntry<DimensionType> theNetherDimensionType = dimensionTypeRegistry.getOrCreateEntry(DimensionTypes.THE_NETHER);
        RegistryEntry<ChunkGeneratorSettings> netherChunkGeneratorSettings = chunkGeneratorSettingsRegistry.getOrCreateEntry(ChunkGeneratorSettings.NETHER);
        DimensionOptions netherDimensionOptions = new DimensionOptions(theNetherDimensionType, new NoiseChunkGenerator(structureSetRegistry, noiseParametersRegistry, MultiNoiseBiomeSource.Preset.NETHER.getBiomeSource(biomeRegistry), netherChunkGeneratorSettings));

        RegistryEntry<DimensionType> theEndDimensionType = dimensionTypeRegistry.getOrCreateEntry(DimensionTypes.THE_END);
        RegistryEntry<ChunkGeneratorSettings> endChunkGeneratorSettings = chunkGeneratorSettingsRegistry.getOrCreateEntry(ChunkGeneratorSettings.END);
        DimensionOptions endDimensionOptions = new DimensionOptions(theEndDimensionType, new NoiseChunkGenerator(structureSetRegistry, noiseParametersRegistry, new TheEndBiomeSource(biomeRegistry), endChunkGeneratorSettings));

        WorldPreset preset = new WorldPreset(Map.of(DimensionOptions.OVERWORLD, dimensionOptions, DimensionOptions.NETHER, netherDimensionOptions, DimensionOptions.END, endDimensionOptions));

        BuiltinRegistries.add(registry, VisiwaGen.ATLAS, preset);
//        todo: inject @ "return" and get locals-- use the MultiNoiseBiomeSource and create a new RegistryEntry<VisiwaBiomeSource>
    }
}
