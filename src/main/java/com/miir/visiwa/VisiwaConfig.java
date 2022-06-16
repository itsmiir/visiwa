/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa;

public class VisiwaConfig {
    public static final int HEIGHT = 256; //  128: 7s; 256: 48s; 512: 381s; 1024x512: 2151s : seems to scale at 2c
    public static final int WIDTH = HEIGHT; // stfu intellij
    public static final int MAX_WORLDGEN_HEIGHT = 256;
    public static final int MOUNTAIN_PEAK_HEIGHT = 190;
    public static final int MOUNTAIN_SLOPE_HEIGHT = 160;
    public static final double HOT_BIOME_TEMP = 200;
    public static final int TEMPERATE_BIOME_TEMP = 100;
    public static final int COLD_BIOME_TEMP = 50;
    public static final int SNOWY_BIOME_TEMP = 0;
    public static final int SEA_LEVEL = 63;
    public static final double BIOME_SIZE = 128;
    public static final int BLURRINESS = 2; // how many times to smooth out noise
    public static float SIGMA = 3f; // spread-outedness of the terrain
    public static final int SCALE_FACTOR = 4; // blocks per pixel (this should be pretty high)
    public static final int HEIGHTMAP_OCTAVES = 6;
    public static final int SUBPIXEL_OCTAVES = 4;
    public static final float THIRSTINESS = 0.1f;
    public static final int SCALE = 16; // how many blocks per pixel of the map
    public static final int BUMPINESS = SCALE * 8;
    public static final double NOISINESS = 10;

    //    mountain constants, fiddling with these is not as fun as it might seem
    public static final float ROUNDNESS = .5f;
    public static final float SHARPNESS = 7f;
    public static final float BUBBLINESS = 4f;
    public static final float PYRAMIDNESS = (ROUNDNESS+SHARPNESS+BUBBLINESS) * 2;

    public static final int SPIKINESS = 17;
    public static int RANGINESS = 30;
    public static final int CRAGGLINESS = 40;
    public static final int FLATNESS = 18;
    public static final int APPALACHIANITY = 12;
    public static float WHORLINESS = .2f;
    public static final int COASTALNESS = 10;
}
