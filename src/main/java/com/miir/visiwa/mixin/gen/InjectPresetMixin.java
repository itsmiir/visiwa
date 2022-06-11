/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.mixin.gen;


import com.google.common.collect.ImmutableList;
import com.miir.visiwa.mixin.VanillaBiomeParametersAccessor;
import com.miir.visiwa.world.biome.source.VisiwaBiomeSource;
import com.miir.visiwa.world.gen.VisiwaGen;
import com.miir.visiwa.world.gen.chunk.VisiwaChunkGeneratorSettings;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.source.util.VanillaBiomeParameters;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldPresets.Registrar.class)
public abstract class InjectPresetMixin {
    @Shadow @Final private Registry<Biome> biomeRegistry;
    @Shadow @Final private Registry<StructureSet> structureSetRegistry;
    @Shadow @Final private Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseParametersRegistry;
    @Shadow protected abstract RegistryEntry<WorldPreset> register(RegistryKey<WorldPreset> key, DimensionOptions dimensionOptions);
    @Shadow protected abstract DimensionOptions createOverworldOptions(ChunkGenerator chunkGenerator);

    @Inject(at = @At("RETURN"), method = "initAndGetDefault")
    private void mixin(CallbackInfoReturnable<RegistryEntry<WorldPreset>> cir) {
        ImmutableList.Builder builder = ImmutableList.builder();
        ((VanillaBiomeParametersAccessor)(Object)new VanillaBiomeParameters()).callWriteVanillaBiomeParameters(pair -> builder.add(pair.mapSecond(biomeRegistry::getOrCreateEntry)));

        this.register(VisiwaGen.ATLAS, this.createOverworldOptions(new NoiseChunkGenerator(
                structureSetRegistry,
                noiseParametersRegistry,
                new VisiwaBiomeSource(new MultiNoiseUtil.Entries(builder.build())),
                BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.getOrCreateEntry(VisiwaChunkGeneratorSettings.ATLAS))));
//        DimensionOptions dimensionOptions = new DimensionOptions(
//                overworldDimensionType,
//                new VisiwaChunkGenerator(
//                        structureSetRegistry,
//                        new VisiwaBiomeSource(biomeRegistry, atlas),
//                        BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.getOrCreateEntry(ChunkGeneratorSettings.OVERWORLD),
//                        noiseParametersRegistry));
//        WorldPreset preset = new WorldPreset(Map.of(DimensionOptions.OVERWORLD, dimensionOptions, DimensionOptions.NETHER, netherDimensionOptions, DimensionOptions.END, endDimensionOptions));
//        BuiltinRegistries.add(worldPresetRegistry, VisiwaGen.ATLAS, preset);
    }
}
