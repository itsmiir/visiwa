/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.world.gen.atlas;

import com.miir.visiwa.Visiwa;
import com.miir.visiwa.VisiwaConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.MathHelper;

public class AtlasSubSampler {
    private static final BlockState AIR = Blocks.AIR.getDefaultState();
    public static BlockState subPixelSample(int absoluteX, int absoluteY, int absoluteZ, int seaLevel, BlockState defaultFluid) {
        int d = MathHelper.floor(getHeight(absoluteX, absoluteZ));
        if (absoluteY > d) {
            if (absoluteY < seaLevel) {
                return defaultFluid;
            }
            return AIR;
        }
        return null;
    }

    public static double getSurfaceNoise(int x, int y, int z) {
        double d = 0;
        for (int n = 0; n < VisiwaConfig.SUBPIXEL_OCTAVES; n++) {
            double e = Visiwa.ATLAS.getSimplex().sample(
                    x/(float)VisiwaConfig.BUMPINESS*(Math.pow(2, n)),
                    z/(float)VisiwaConfig.BUMPINESS*(Math.pow(2, n)));
            e *= (VisiwaConfig.NOISINESS * getSubpixelNoise(x, y, z)) / (Math.pow(2, n+1));
            d += e;
        }
        return d;
    }

    private static double getSubpixelNoise(int x, int y, int z) {
        int[] pos = AtlasHelper.coordToPixel(x, y, z);
        return Visiwa.ATLAS.sampleSubpixelNoise(x, y, z);
    }

    public static double getHeight(int x, int z) {
        if (Visiwa.ATLAS.isNotBuilt()) {

            throw new IllegalStateException("tried to perform an operation on an atlas that has not been built!");
        }
        int[] pos = AtlasHelper.coordToPixel(x, 0, z);
        int xPx = pos[0];
        int zPx = pos[2];
        double y = Visiwa.ATLAS.lerpElevation(xPx, zPx, x, z);
        double height = AtlasSubSampler.getSurfaceNoise(x, (int) Math.round(y), z);
        return Visiwa.ATLAS.transformHeight(x, z, height, y, VisiwaConfig.VERTICAL_SCALE) + 100;
    }
}
