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
import com.miir.visiwa.VisiwaConfig;
import com.miir.visiwa.world.gen.Atlas;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MultiNoiseUtil.MultiNoiseSampler.class)
//this will work trust me
public class FixEverythingInMyLifeMixin {

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
            i/=(VisiwaConfig.SCALE);
//            y coordinate should not be scaled
            k/=(VisiwaConfig.SCALE);
            /*
            todo: elevation is too high (oceans are not common enough) transform elevation w/ sharper function? e.g. 7e^(2x-4) on (-1, 1)
            todo: get hands on biome lookup table
            todo: implement slight noise variations so biomes aren't too terribly huge (and also so biomes repeat a little more)
            todo: chunk generator still is building heightmap based on vanilla noise params [citation needed] (ocean biomes can generate atop mountains)
            todo: change overwrite to more friendly mixin type (conditional redirect?)
             */
            return MultiNoiseUtil.createNoiseValuePoint(
                    Visiwa.ATLAS.getFloatNoiseVal(Visiwa.ATLAS.getT(i, k)),
                    Visiwa.ATLAS.getFloatNoiseVal(Visiwa.ATLAS.getH(i, k)),
                    Visiwa.ATLAS.getFloatNoiseVal(Visiwa.ATLAS.getY(i, k)),
                    -Visiwa.ATLAS.getFloatNoiseVal(Visiwa.ATLAS.getY(i, k)),
                    (float) this.depth.sample(new DensityFunction.UnblendedNoisePos(i, j, k)),
                    Visiwa.ATLAS.getFloatNoiseVal(Visiwa.ATLAS.getW(i, k))
                    );
        }
        return MultiNoiseUtil.createNoiseValuePoint((float)this.temperature.sample(unblendedNoisePos), (float)this.humidity.sample(unblendedNoisePos), (float)this.continentalness.sample(unblendedNoisePos), (float)this.erosion.sample(unblendedNoisePos), (float)this.depth.sample(unblendedNoisePos), (float)this.weirdness.sample(unblendedNoisePos));
    }

}
