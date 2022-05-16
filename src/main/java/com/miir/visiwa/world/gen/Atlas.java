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
    public final boolean pirate_adventure;
    public final boolean regional; // creates a realistic (life-sized) map, but with a limited climate range
    public final int scale_factor;
    private final int width;
    private final int height;
    private AtlasPixel[][] map;
    private final ArrayList<Point> land = new ArrayList<>();
    private final ArrayList<Point> water = new ArrayList<>();
    private final SimpleRandom random;
    private final SimplexNoiseSampler simplex;
    public float completion;
    private boolean built = false;

//    order of elements in the map is erosion, peaks/valleys, humidity, temperature, continentalness, weirdness
    public Atlas(long seed, int scale_factor, boolean pirate_adventure, boolean regional, int width, int height) {
        this.seed = seed;
        this.scale_factor = scale_factor;
        this.pirate_adventure = pirate_adventure;
        this.regional = regional;
        this.width = width;
        this.height = height;
        this.random = new SimpleRandom(seed);
        this.simplex = new SimplexNoiseSampler(this.random);
        this.completion = 1f;
        this.map = new AtlasPixel[height][width];
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

    public void simplexTerrain() {
        this.completion = 0f;
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                float pos = 0;
                float neg = 0;
                float z = 0;
                for (int n = 0; n < Visiwa.COUNT+1; n++) {
                    pos += (this.simplex.sample(x/128.0/this.scale_factor*(Math.pow(2, n)), y/128.0/this.scale_factor*(Math.pow(2, n)))+.5)/(2*Math.pow(2, n));
                    neg += (this.simplex.sample(x/32.0/this.scale_factor*(Math.pow(2, n)), y/32.0/this.scale_factor*(Math.pow(2, n)))-1)/(2*Math.pow(2, n));
                    z = MathHelper.clamp((pos+neg)/2.0f+(VisiwaConfig.THIRSTINESS), -1.0f, 1.0f);
                }
                z /= 2;
                z += .5f;
                z *= 256;
                AtlasPixel px = new AtlasPixel();
                if (z <= 128) {
                    this.water.add(new Point(x, y));
                } else {
                    this.land.add(new Point(x, y));
                }
                px.y = AtlasHelper.clean(z);
                px.c = AtlasHelper.clean(z);
                this.map[y][x] = px;
            }
        }
        this.built = true;
    }

    public void elevation() {
        ArrayList<Point> rangePts = new ArrayList<>();
        if (land.size() == 0) {
            return;
        }
        for (int x = 0; x < this.random.nextGaussian() * VisiwaConfig.SIGMA + VisiwaConfig.RANGINESS * this.width / 128f; x++) {
            Point pt0 = randomPoints(this.getLand(), 1).get(0);
            double a = this.random.nextFloat()*Math.PI*2;
            for (int y = 0; y < this.random.nextGaussian() * VisiwaConfig.CRAGGLINESS / 3 + VisiwaConfig.CRAGGLINESS; y++) {
                for (int z = 0; z < 100; z++) {
                    Point pt1 = randomPoints(this.land, 1).get(0);
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
        for (Point point : this.land) {
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
            int h3 = 255 - AtlasHelper.clean(255*this.simplex.sample(ref.x/256.0, ref.y/256.0));
            int h4 = this.getPixel(point).y;
            double h = (h1*VisiwaConfig.ROUNDNESS + h2*VisiwaConfig.SHARPNESS + h3*VisiwaConfig.BUBBLINESS + h4*VisiwaConfig.PYRAMIDNESS)
                    / ((VisiwaConfig.BUBBLINESS + VisiwaConfig.ROUNDNESS + VisiwaConfig.SHARPNESS + VisiwaConfig.PYRAMIDNESS)*255);
            h /= 2.0;
            h += .5;
            this.map[point.y][point.x].y = AtlasHelper.clean(h*256);
        }

    }

    public void continentiality() {
        for (Point point : this.land) {
            Point nearestOcean;
            ArrayList<Point> neighbors = pointsWithinRange(point, VisiwaConfig.COASTALNESS);
            ArrayList<Point> oceanPoints = new ArrayList<>();
            neighbors.forEach((p1 -> {
                if (this.getPixel(p1).y < 128) {
                    oceanPoints.add(p1);
                }
            }));
            if (oceanPoints.size() == 0) {
                this.map[point.y][point.x].c = AtlasHelper.clean(255); // if i ever change this to be a byte it'll make this bit easier
            } else {
                nearestOcean = AtlasHelper.findClosestPoint(point, oceanPoints)[0];
                this.map[point.y][point.x].c = AtlasHelper.clean(255.0 * ((nearestOcean.distance(point)/VisiwaConfig.COASTALNESS)/2.0 + 0.5));
            }
        }
    }

    public void drawMap(AtlasHelper.PARAM p) throws IOException {
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
                    float h = v/256.0f;
                    Color c = AtlasHelper.colorMap((h-0.5f)*2, p);
                    img.setRGB(x, y, c.getRGB());
                }
            }
            String path = "output" + ((Integer)Visiwa.COUNT);
            ImageIO.write(img, "png", new File(path + ".png"));
            System.out.println("Image generated");
        }
    }

    private AtlasPixel getPixel(Point point) {
        return this.map[point.y][point.x];
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

    private ArrayList<Point> getLand() {
        ArrayList<Point> land = new ArrayList<>();
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                if (this.map[y][x].y > 128) {
                    land.add(new Point(x, y));
                }
            }
        }
        return land;
    }
}

