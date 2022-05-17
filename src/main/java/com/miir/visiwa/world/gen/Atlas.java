/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.world.gen;

import com.miir.visiwa.Visiwa;
import com.miir.visiwa.VisiwaConfig;
import com.miir.visiwa.world.gen.AtlasHelper.AtlasPixel;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.gen.random.SimpleRandom;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class Atlas implements Serializable {
    private final long seed;
    public final boolean regional; // creates a realistic (life-sized) map, but with a limited climate range
    public final int scale_factor;
    private final int width;
    private final int height;
    private final AtlasPixel[][] map;
    private final SimpleRandom random;
    private final SimplexNoiseSampler simplex;
    public float completion;
    private boolean built = false;
    private final boolean north;

//    order of elements in the map is erosion, peaks/valleys, humidity, temperature, continentalness, weirdness
    public Atlas(long seed, int scale_factor, boolean regional, int width, int height) {
        this.seed = seed;
        this.scale_factor = scale_factor;
        this.regional = regional;
        this.width = width;
        this.height = height;
        this.random = new SimpleRandom(seed);
        this.simplex = new SimplexNoiseSampler(this.random);
        this.completion = 1f;
        this.map = new AtlasPixel[height][width];
        this.north = this.random.nextBoolean();
    }

    public long getSeed() {
        return this.seed;
    }

    public void simplexTerrain() {
        this.completion = 0f;
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                float pos = 0;
                float neg = 0;
                float z = 0;
                for (int n = 0; n < VisiwaConfig.HEIGHTMAP_OCTAVES; n++) {
                    pos += (this.simplex.sample(x/128.0/this.scale_factor*(Math.pow(2, n)), y/128.0/this.scale_factor*(Math.pow(2, n)))+.5)/(2*Math.pow(2, n));
                    neg += (this.simplex.sample(x/32.0/this.scale_factor*(Math.pow(2, n)), y/32.0/this.scale_factor*(Math.pow(2, n)))-1)/(2*Math.pow(2, n));
                    z = MathHelper.clamp((pos+neg)/2.0f+(VisiwaConfig.THIRSTINESS), -1.0f, 1.0f);
                }
                z /= 2;
                z += .5f;
                z *= 255;
                AtlasPixel px = new AtlasPixel();
                px.y = AtlasHelper.clean(z);
                px.c = AtlasHelper.clean(z);
                this.map[y][x] = px;
            }
        }
        this.built = true;
    }

    public void elevation() {
        ArrayList<Point> rangePts = new ArrayList<>();
        ArrayList<Point> land = this.getLand();
        if (land.size() == 0) {
            return;
        }
        for (int x = 0; x < this.random.nextGaussian() * VisiwaConfig.SIGMA + VisiwaConfig.RANGINESS * this.width / 128f; x++) {
            Point pt0 = randomPoints(land, 1).get(0);
            double a = this.random.nextFloat()*Math.PI*2;
            for (int y = 0; y < this.random.nextGaussian() * VisiwaConfig.CRAGGLINESS / 3 + VisiwaConfig.CRAGGLINESS; y++) {
                for (int z = 0; z < 100; z++) {
                    Point pt1 = randomPoints(land, 1).get(0);
                    if (pt0.distance(pt1) < VisiwaConfig.APPALACHIANITY) {
                        double a2 = AtlasHelper.getAngle(pt0, pt1);
                        if (a2 < a + VisiwaConfig.WHORLINESS && a2 > a - VisiwaConfig.WHORLINESS) {
                            rangePts.add(pt1);
                            rangePts.add(new Point(
                                            MathHelper.clamp(pt1.x + this.random.nextInt(VisiwaConfig.SPIKINESS), 0, this.width - 1),
                                            MathHelper.clamp(pt1.y + this.random.nextInt(VisiwaConfig.SPIKINESS), 0, this.height - 1)));
                            pt0 = pt1;
                            a = a2;
                            break;
                        }
                    }
                }
            }
        }
        for (Point point : land) {
            Point ref;
            if (rangePts.contains(point)) {
                ref = new Point(point.x, MathHelper.clamp(point.y+1, 0, this.height-1));
            } else {
                ref = point;
            }
            Point[] anchor = AtlasHelper.findClosestPoint(ref, rangePts, 2);
            Point anchor1 = anchor[0];
            Point anchor2 = anchor[1];
            double d1 = ref.distance(anchor1);
            double d2 = ref.distance(anchor2);
            int h1 = 255 - AtlasHelper.clean(255*d1*VisiwaConfig.FLATNESS/width);
            int h2 = 255 - AtlasHelper.clean(255*d2*VisiwaConfig.FLATNESS/width);
            int h3 = 255 - AtlasHelper.clean(255*this.simplex.sample(ref.x/255.0, ref.y/255.0));
            int h4 = this.getPixel(point).y;
            double h = (h1*VisiwaConfig.ROUNDNESS + h2*VisiwaConfig.SHARPNESS + h3*VisiwaConfig.BUBBLINESS + h4*VisiwaConfig.PYRAMIDNESS)
                    / ((VisiwaConfig.BUBBLINESS + VisiwaConfig.ROUNDNESS + VisiwaConfig.SHARPNESS + VisiwaConfig.PYRAMIDNESS));
            h /= 2.0;
            h += 127;
            this.map[point.y][point.x].y = AtlasHelper.clean(h);
        }

    }

    public void continentalness() {
        double n = Math.pow(VisiwaConfig.COASTALNESS*2.0+1.0, 2);
        for (Point point : this.getLand()) {
            ArrayList<Point> neighbors = pointsWithinRange(point, VisiwaConfig.COASTALNESS);
            int oceanPoints = (int) neighbors.stream().filter(p1 -> this.getPixel(p1).y < 128).count();
            this.map[point.y][point.x].c = AtlasHelper.clean(255 * (((n-oceanPoints)/n)/2.0 + 0.5));
        }
    }

    public void temperature() {
        if (!this.regional) {
            if (this.north) {
                for (int y = 0; y < this.height; y++) {
                    for (int x = 0; x < this.width; x++) {
                        int g = 0xFF - y;
                        int h = (int) (31*this.simplex.sample(x/255.0, y/255.0));
                        this.getPixel(x, y).t = AtlasHelper.clean((g + h) / 1.125);
                    }
                }
            } else {
                for (int y = 0; y < this.height; y++) {
                    for (int x = 0; x < this.width; x++) {
                        int h = (int) (31*this.simplex.sample(x/255.0, y/255.0));
                        this.getPixel(x, y).t = AtlasHelper.clean((y + h) / 1.125);
                    }
                }
            }


        }
    }

    public void airflow() {
//        TODO: fix this (low airflow on continents, high everywhere else)
        if (!this.regional) {
            if (false) {
                for (int y = 1; y < this.height - 2; y++) {
                    for (int x = 1; x < this.width - 2; x++) {
                        AtlasPixel pixel = this.getPixel(x, y);
                        if (pixel.isLand()) {
                            AtlasPixel pixel2 = this.getPixel(x, y-1);
                            pixel.a = AtlasHelper.clean(pixel2.a * (1-(pixel2.y-pixel.y)/2.0));
                        }
                    }
                }
                for (int y = 1; y < this.height - 1; y++) {
                    for (int x = 1; x < this.width - 1; x++) {
                        AtlasPixel pixel = this.getPixel(x, y);
                        pixel.a =
                                AtlasHelper.clean((pixel.a +
                                        this.getPixel(x-1, y-1).a +
                                        this.getPixel(x, y-1).a +
                                        this.getPixel(x+1, y-1).a +
                                        this.getPixel(x-1, y).a +
                                        this.getPixel(x+1, y).a +
                                        this.getPixel(x-1, y+1).a +
                                        this.getPixel(x, y+1).a +
                                        this.getPixel(x+1, y+1).a) / 9.0);
                    }
                }
            } else {
                int n = 0;
                float[] elevs = new float[this.width - 1];
                for (int y = this.height - 2; y > 0; y--) {
                    for (int x = 1; x < this.width - 1; x++) {
                        AtlasPixel pixel = this.getPixel(x, y);
                        if (pixel.isLand()) {
                            AtlasPixel pixel2 = this.getPixel(x, y+1);
                            elevs[x] += pixel.y;
                            n++;
                            pixel.a = AtlasHelper.clean(pixel2.a*(elevs[x]/n-128)*2.0);
                        }
                    }
                }
                for (int y = this.height - 2; y > 0; y--) {
                    for (int x = 1; x < this.width - 1; x++) {
                        AtlasPixel pixel = this.getPixel(x, y);
                        pixel.a =
                                AtlasHelper.clean((pixel.a +
                                        this.getPixel(x-1, y-1).a +
                                        this.getPixel(x, y-1).a +
                                        this.getPixel(x+1, y-1).a +
                                        this.getPixel(x-1, y).a +
                                        this.getPixel(x+1, y).a +
                                        this.getPixel(x-1, y+1).a +
                                        this.getPixel(x, y+1).a +
                                        this.getPixel(x+1, y+1).a) / 9.0);
                    }
                }
            }
        }
    }

    public void drawMap(AtlasHelper.PARAM p, String path) throws IOException {
        if (this.built) {
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int v;
                    switch (p) {
                        case CONTINENTALNESS -> v = (this.map[y][x].c & 0xFF);
                        case TEMPERATURE -> v = (this.map[y][x].t & 0xFF);
                        case AIRFLOW -> v = (this.map[y][x].a & 0xFF);
                        case PEAKS -> v = (this.map[y][x].pv & 0xFF);
                        case EROSION -> v = (this.map[y][x].e & 0xFF);
                        case HUMIDITY -> v = (this.map[y][x].h & 0xFF);
                        case WEIRDNESS -> v = (this.map[y][x].w & 0xFF);
                        default -> v = (this.map[y][x].y & 0xFF);
                    }
                    float h = v/255.0f;
                    int c = AtlasHelper.colorMap((h-0.5f)*2, p);
                    img.setRGB(x, y, c);
                }
            }
            ImageIO.write(img, "png", new File(path + ".png"));
            System.out.println("Image generated");
        }
    }

    private AtlasPixel getPixel(Point point) {
        return this.map[point.y][point.x];
    }
    private AtlasPixel getPixel(int x, int y) {
        return this.map[y][x];
    }

    private Point randomPoint() {
        int x = (int) Math.round(this.random.nextGaussian() * (this.width/VisiwaConfig.SIGMA) + (this.width/2.0f));
        int y = (int) Math.round(this.random.nextGaussian() * (this.height/VisiwaConfig.SIGMA) + (this.height/2.0f));
        return new Point(x, y);
    }
    private ArrayList<Point> randomPoints(ArrayList<Point> points, int k) {
        if (k > points.size()) {
            return points;
        }
        ArrayList<Point> randoms = new ArrayList<>();
        while (randoms.size() < k) {
            Point p = points.get(this.random.nextInt(points.size()));
            if (!randoms.contains(p)) {
                randoms.add(p);
            }
        }
        return randoms;
    }
    public ArrayList<Point> pointsWithinRange(Point point, int radius) {
        ArrayList<Point> points = new ArrayList<>();
        for (int y = 0; y <= radius*2+1; y++) {
            for (int x = 0; x <= radius*2+1; x++) {
                Point c = new Point(MathHelper.clamp(x-radius + point.x, 0, this.width - 1), MathHelper.clamp(y-radius + point.y, 0, this.height - 1));
                if (!points.contains(c)) {
                    points.add(c);
                }
            }
        }
        return points;
    }

    private ArrayList<Point> getLand() {
        ArrayList<Point> land = new ArrayList<>();
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                if (this.map[y][x].y > 127) {
                    land.add(new Point(x, y));
                }
            }
        }
        return land;
    }
}

