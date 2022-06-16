/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.mixin.gen.biome;

import com.miir.visiwa.Visiwa;
import com.miir.visiwa.world.gen.atlas.AtlasHelper;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MultiNoiseUtil.MultiNoiseSampler.class)
public class PaintBiomesMixin {

    @Shadow @Final private DensityFunction temperature;

    @Shadow @Final private DensityFunction humidity;

    @Shadow @Final private DensityFunction continentalness;

    @Shadow @Final private DensityFunction erosion;

    @Shadow @Final private DensityFunction depth;

    @Shadow @Final private DensityFunction weirdness;

    /**
     * @author miir
     * @reason theoretically, should not interfere with other mods :)
     */
    @Overwrite
    public MultiNoiseUtil.NoiseValuePoint sample(int x, int y, int z) {
        int i = BiomeCoords.toBlock(x);
        int j = BiomeCoords.toBlock(y);
        int k = BiomeCoords.toBlock(z);
        DensityFunction.UnblendedNoisePos unblendedNoisePos = new DensityFunction.UnblendedNoisePos(i, j, k);
        if (Visiwa.isAtlas) {
            int[] ints = AtlasHelper.coordToPixel(i, j, k);
//            y coordinate should not be scaled
            int xPx = ints[0];
            int zPx = ints[2];
            return MultiNoiseUtil.createNoiseValuePoint(
                    Visiwa.ATLAS.getFloatNoiseVal((int) Math.round(Visiwa.ATLAS.lerpTemp(xPx, zPx, i, k))),
                    Visiwa.ATLAS.getFloatNoiseVal((int) Math.round(Visiwa.ATLAS.lerpHumidity(xPx, zPx, i, k))),
                    Visiwa.ATLAS.getFloatNoiseVal((int) Math.round(Visiwa.ATLAS.lerpElevation(xPx, zPx, i, k))),
                    -Visiwa.ATLAS.getFloatNoiseVal((int) Math.round(Visiwa.ATLAS.getY(xPx, zPx))),
                    (float) this.depth.sample(unblendedNoisePos),
                    Visiwa.ATLAS.getFloatNoiseVal((int) Math.round(Visiwa.ATLAS.lerpWeirdness(xPx, zPx, i, k)))
                    );
        }
        return MultiNoiseUtil.createNoiseValuePoint((float)this.temperature.sample(unblendedNoisePos), (float)this.humidity.sample(unblendedNoisePos), (float)this.continentalness.sample(unblendedNoisePos), (float)this.erosion.sample(unblendedNoisePos), (float)this.depth.sample(unblendedNoisePos), (float)this.weirdness.sample(unblendedNoisePos));
    }

}
