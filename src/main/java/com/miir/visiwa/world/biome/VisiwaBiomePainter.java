/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.world.biome;

import com.miir.visiwa.Visiwa;
import com.miir.visiwa.VisiwaConfig;
import com.miir.visiwa.world.gen.atlas.AtlasHelper;
import com.miir.visiwa.world.gen.atlas.AtlasSubSampler;
import net.minecraft.tag.BiomeTags;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VisiwaBiomePainter {
    public static Optional<RegistryEntry<Biome>> getBiome(int x, int y, int z) {

        ArrayList<RegistryEntry<Biome>> biomes = new ArrayList<>(Visiwa.BIOMES);
        int[] pixel = AtlasHelper.coordToPixel(x, y, z);
        double weirdness = Visiwa.ATLAS.lerpWeirdness(pixel[0], pixel[2], x, z);
        double height = AtlasSubSampler.getHeight(x, z);
        double temperature = Visiwa.ATLAS.lerpTemp(pixel[0], pixel[2], x, z);
        double biomeNoise = Visiwa.ATLAS.lerpBiomeNoise(pixel[0], pixel[2], x, z);
        if (Visiwa.DEBUG_BIOME) {
            if (height < 105) {
                biomes.removeIf(biome -> !biome.getKey().get().equals(BiomeKeys.OLD_GROWTH_PINE_TAIGA));
            } else {
                biomes.removeIf(biome -> !biome.getKey().get().equals(BiomeKeys.SNOWY_SLOPES));
            }
            return Optional.of(biomes.get(0));
        }
        if (height >= VisiwaConfig.MOUNTAIN_PEAK_HEIGHT + Visiwa.ATLAS.getSimplex().sample(x/255.0, y/255.0, z/255.0)*7) {
            biomes.removeIf(biome -> !biome.getKey().get().getValue().getPath().contains("peaks"));
        } else if (height >= VisiwaConfig.MOUNTAIN_SLOPE_HEIGHT + Visiwa.ATLAS.getSimplex().sample(x/255.0, y/255.0, z/255.0)*7) {
            biomes.removeIf(biome -> !biome.isIn(BiomeTags.IS_MOUNTAIN));
        } else if (height <= 30) {
            biomes.removeIf(biome -> !biome.isIn(BiomeTags.IS_DEEP_OCEAN));
        } else if (height <= 55) {
            biomes.removeIf(biome -> !biome.isIn(BiomeTags.IS_OCEAN) || biome.isIn(BiomeTags.IS_DEEP_OCEAN));
        } else if (height <= 67) {
            biomes.removeIf(biome -> !biome.isIn(BiomeTags.IS_BEACH));
        }
        else {
//            none of the above biomes should spawn outside those spots
            biomes.removeIf(biome -> (biome.isIn(BiomeTags.IS_RIVER) || biome.isIn(BiomeTags.IS_OCEAN) || biome.isIn(BiomeTags.IS_BEACH) || biome.isIn(BiomeTags.IS_MOUNTAIN)));
            if (temperature >= VisiwaConfig.HOT_BIOME_TEMP) {
                biomes.removeIf(biome -> !(biome.value().getTemperature() >= 1));
            } else if (temperature >= VisiwaConfig.TEMPERATE_BIOME_TEMP) {
                biomes.removeIf(biome -> !(biome.value().getTemperature() <= 1 && biome.value().getTemperature() >= 0.5 && !biome.isIn(BiomeTags.IS_OCEAN)));
            } else if (temperature >= VisiwaConfig.COLD_BIOME_TEMP) {
                biomes.removeIf(biome -> !(biome.value().getTemperature() < 0.5 && biome.value().getTemperature() >= 0.1));
            } else {
                biomes.removeIf(biome -> !(biome.value().getTemperature() <= 0.1));
            }
        }
        int n = biomes.size();
        if (n == 0) {
            return Optional.empty();
        } else {
            weirdness /= 255;
            weirdness *= n;
            weirdness = Math.round(weirdness);
            try {
//                Visiwa.LOGGER.info("weirdness value: " + (int) weirdness);
                return Optional.of(((List<RegistryEntry<Biome>>) biomes).get((int) weirdness));
            } catch (IndexOutOfBoundsException e) {
                return Optional.of(biomes.get(0));
            }
        }
    }
}
