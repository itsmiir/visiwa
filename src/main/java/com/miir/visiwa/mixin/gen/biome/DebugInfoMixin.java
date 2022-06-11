/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.mixin.gen.biome;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.source.util.VanillaBiomeParameters;
import net.minecraft.world.gen.densityfunction.DensityFunctions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@Mixin(MultiNoiseBiomeSource.class)
public class DebugInfoMixin {
    /**
     * @author
     */
    @Overwrite
    public void addDebugInfo(List<String> info, BlockPos pos, MultiNoiseUtil.MultiNoiseSampler noiseSampler) {
        int i = BiomeCoords.fromBlock(pos.getX());
        int j = BiomeCoords.fromBlock(pos.getY());
        int k = BiomeCoords.fromBlock(pos.getZ());
        MultiNoiseUtil.NoiseValuePoint noiseValuePoint = noiseSampler.sample(i, j, k);
        float continentalnessNoise = noiseValuePoint.continentalnessNoise();
        float erosionNoise = noiseValuePoint.erosionNoise();
        float temperatureNoise = noiseValuePoint.temperatureNoise();
        float humidityNoise = noiseValuePoint.humidityNoise();
        float weirdnessNoise = noiseValuePoint.weirdnessNoise();
        info.add("W: " + weirdnessNoise + " C: " + continentalnessNoise + " E: " + erosionNoise + " T: " + temperatureNoise + " H: " + humidityNoise);
    }
}
