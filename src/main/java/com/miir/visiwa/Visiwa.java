/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa;

import com.miir.visiwa.world.gen.Atlas;
import com.miir.visiwa.world.gen.AtlasHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.tinyremapper.extension.mixin.common.Logger;

import java.io.IOException;
import java.util.Random;

public class Visiwa implements ModInitializer {
//    public static final Logger VISIWA_GENERATOR = new Logger(Logger.Level.INFO);
    public static int COUNT = 0;
    @Override
    public void onInitialize() {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            Atlas atlas = new Atlas(random.nextLong(), 2, false, false, VisiwaConfig.WIDTH, VisiwaConfig.HEIGHT);
            atlas.simplexTerrain();
            atlas.elevation();
            atlas.continentiality();
            try {
                atlas.drawMap(AtlasHelper.PARAM.CONTINENTALNESS);
            } catch (IOException e) {
                e.printStackTrace();
            }
            COUNT++;
        }

    }
}
