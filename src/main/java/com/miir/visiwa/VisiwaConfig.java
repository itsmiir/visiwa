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
    public static float SIGMA = 3f; // spread-outedness of the terrain
    public static final int DENSITY = 6; // higher density == more water nodes
    public static final float WETNESS = 0.004f; // self-explanatory
    public static float OCEAN_SIZE = 0.55f; // higher == more ocean (don't set this too low)
    public static final int K = 1; // probably get rid of this
    public static final boolean PIRATE_ADVENTURE = false; // arr
    public static final int SCALE_FACTOR = 4; // chunks per pixel
    public static final int HEIGHTMAP_OCTAVES = 6;
    public static final float THIRSTINESS = 0.1f;
    //    terrain constants, fiddling with these is not as fun as it might seem
    public static int ROCKINESS = 64;
    public static final float ROUNDNESS = .5f;
    public static final float SHARPNESS = 7f;
    public static final float BUBBLINESS = 4f;
    public static final float PYRAMIDNESS = ROUNDNESS+SHARPNESS+BUBBLINESS;

    public static final int SPIKINESS = 17;
    public static int RANGINESS = 10;
    public static final int CRAGGLINESS = 40;
    public static final int FLATNESS = 18;
    public static final int APPALACHIANITY = 12;
    public static float WHORLINESS = .2f;
    public static final int COASTALNESS = 10;

}
