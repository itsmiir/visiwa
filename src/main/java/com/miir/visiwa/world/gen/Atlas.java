/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.world.gen;

import com.miir.visiwa.VisiwaConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Atlas implements Serializable {
    public static final Codec<Atlas> CODEC;
    public static final int DEPTH = 8; // how many values each pixel of the atlas has
    private final long seed;
    public final boolean regional; // creates a realistic (life-sized) map, but with a limited climate range
    public final int scaleFactor;
    private final int width;
    private final int height;
    private final net.minecraft.util.math.random.Random random;
    private final SimplexNoiseSampler simplex;
    private int[][][] map;
    private boolean north;
    public float completion;
    private boolean built = false;

//    order of elements in the map is erosion, peaks/valleys, humidity, temperature, continentalness, weirdness
    public Atlas(long seed, int scale_factor, boolean regional, int width, int height) {
        this.seed = seed;
        this.scaleFactor = scale_factor;
        this.regional = regional;
        this.width = width;
        this.height = height;
        this.random = new ChunkRandom(new CheckedRandom(seed));
        this.simplex = new SimplexNoiseSampler(this.random);
        this.completion = 1f;
        this.map = new int[height][width][DEPTH];
        this.north = this.random.nextBoolean();
    }
    public Atlas(long seed, int scale_factor, boolean regional, int width, int height, boolean north, IntStream map) {
        this(seed, scale_factor, regional, width, height);
        this.north = north;
        this.map = this.mapFromStream(this.getMapStream());
    }
    public Atlas(long seed) {
        this.random = new ChunkRandom(new CheckedRandom(seed));
        this.seed = seed;
        this.simplex = new SimplexNoiseSampler(this.random);
        this.width = VisiwaConfig.WIDTH;
        this.height = VisiwaConfig.HEIGHT;
        this.scaleFactor = VisiwaConfig.SCALE_FACTOR;
        this.north = random.nextBoolean();
        this.regional = random.nextBoolean();
        this.map = new int[height][width][DEPTH];
    }

    public static IntStream mapToStream(int[][][] map) {
        return Arrays.stream(map).flatMap(Stream::of).flatMapToInt(IntStream::of);
    }
    public int[][][] mapFromStream(IntStream map) {
        int y = 0;
        int x = 0;
        int z = 0;
//        because this is run after constructor, height and width will not be null
//        ^^ not sure what past me was on about but okay ig
        int[][][] newMap = new int[this.height][this.width][Atlas.DEPTH];
        for (int i : map.toArray()) {
            newMap[y][x][z] = i;
            z++;
            if (z >= Atlas.DEPTH) {
                z = 0;
                x++;
            }
            if (x >= this.width) {
                x = 0;
                y++;
            }
        }
        return newMap;
    }

    public long getSeed() {
        return this.seed;
    }
    public int getScaleFactor() {
        return this.scaleFactor;
    }
    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }
    public IntStream getMapStream() {
        return mapToStream(this.map);
    }

    public int getY(Point point) {
        return this.getMap()[MathHelper.clamp(point.y, 0, this.height)][MathHelper.clamp(point.x, 0, this.width)][0];
    }
    public int getY(int x, int y) {
        return this.getMap()[MathHelper.clamp(y, 0, this.height)][MathHelper.clamp(x, 0, this.width)][0];
    }
    public int getC(Point point) {
        return this.getMap()[MathHelper.clamp(point.y, 0, this.height)][MathHelper.clamp(point.x, 0, this.width)][1];
    }
    public int getC(int x, int y) {
        return this.getMap()[MathHelper.clamp(y, 0, this.height)][MathHelper.clamp(x, 0, this.width)][1];
    }
    public int getT(Point point) {
        return this.getMap()[MathHelper.clamp(point.y, 0, this.height)][MathHelper.clamp(point.x, 0, this.width)][2];
    }
    public int getT(int x, int y) {
        return this.getMap()[MathHelper.clamp(y, 0, this.height)][MathHelper.clamp(x, 0, this.width)][2];
    }
    public int getA(Point point) {
        return this.getMap()[MathHelper.clamp(point.y, 0, this.height)][MathHelper.clamp(point.x, 0, this.width)][3];
    }
    public int getA(int x, int y) {
        return this.getMap()[MathHelper.clamp(y, 0, this.height)][MathHelper.clamp(x, 0, this.width)][3];
    }
    public int getH(Point point) {
        return this.getMap()[MathHelper.clamp(point.y, 0, this.height)][MathHelper.clamp(point.x, 0, this.width)][4];
    }
    public int getH(int x, int y) {
        return this.getMap()[MathHelper.clamp(y, 0, this.height)][MathHelper.clamp(x, 0, this.width)][4];
    }
    public int getE(Point point) {
        return this.getMap()[MathHelper.clamp(point.y, 0, this.height)][MathHelper.clamp(point.x, 0, this.width)][5];
    }
    public int getE(int x, int y) {
        return this.getMap()[MathHelper.clamp(y, 0, this.height)][MathHelper.clamp(x, 0, this.width)][5];
    }
    public int getPV(Point point) {
        return this.getMap()[MathHelper.clamp(point.y, 0, this.height)][MathHelper.clamp(point.x, 0, this.width)][6];
    }
    public int getPV(int x, int y) {
        return this.getMap()[MathHelper.clamp(y, 0, this.height)][MathHelper.clamp(x, 0, this.width)][6];
    }
    public int getW(Point point) {
        return this.getMap()[MathHelper.clamp(point.y, 0, this.height)][MathHelper.clamp(point.x, 0, this.width)][7];
    }
    public int getW(int x, int y) {
        return this.getMap()[MathHelper.clamp(y, 0, this.height)][MathHelper.clamp(x, 0, this.width)][7];
    }

    private void setY(int i, Point point) {
        this.map[MathHelper.clamp(point.y, 0, this.height)][MathHelper.clamp(point.x, 0, this.width)][0] = i;
    }
    private void setY(int i, int x, int y) {
        this.map[MathHelper.clamp(y, 0, this.height)][MathHelper.clamp(x, 0, this.width)][0] = i;
    }
    private void setC(int i, Point point) {
        this.map[MathHelper.clamp(point.y, 0, this.height)][MathHelper.clamp(point.x, 0, this.width)][1] = i;
    }
    private void setC(int i, int x, int y) {
        this.map[MathHelper.clamp(y, 0, this.height)][MathHelper.clamp(x, 0, this.width)][1] = i;
    }
    private void setT(int i, Point point) {
        this.map[MathHelper.clamp(point.y, 0, this.height)][MathHelper.clamp(point.x, 0, this.width)][2] = i;
    }
    private void setT(int i, int x, int y) {
        this.map[MathHelper.clamp(y, 0, this.height)][MathHelper.clamp(x, 0, this.width)][2] = i;
    }
    private void setA(int i, Point point) {
        this.map[MathHelper.clamp(point.y, 0, this.height)][MathHelper.clamp(point.x, 0, this.width)][3] = i;
    }
    private void setA(int i, int x, int y) {
        this.map[MathHelper.clamp(y, 0, this.height)][MathHelper.clamp(x, 0, this.width)][3] = i;
    }
    private void setH(int i, Point point) {
        this.map[MathHelper.clamp(point.y, 0, this.height)][MathHelper.clamp(point.x, 0, this.width)][4] = i;
    }
    private void setH(int i, int x, int y) {
        this.map[MathHelper.clamp(y, 0, this.height)][MathHelper.clamp(x, 0, this.width)][4] = i;
    }
    private void setE(int i, Point point) {
        this.map[MathHelper.clamp(point.y, 0, this.height)][MathHelper.clamp(point.x, 0, this.width)][5] = i;
    }
    private void setE(int i, int x, int y) {
        this.map[MathHelper.clamp(y, 0, this.height)][MathHelper.clamp(x, 0, this.width)][5] = i;
    }
    private void setPV(int i, Point point) {
        this.map[MathHelper.clamp(point.y, 0, this.height)][MathHelper.clamp(point.x, 0, this.width)][6] = i;
    }
    private void setPV(int i, int x, int y) {
        this.map[MathHelper.clamp(y, 0, this.height)][MathHelper.clamp(x, 0, this.width)][6] = i;
    }
    private void setW(int i, Point point) {
        this.map[MathHelper.clamp(point.y, 0, this.height)][MathHelper.clamp(point.x, 0, this.width)][7] = i;
    }
    private void setW(int i, int x, int y) {
        this.map[MathHelper.clamp(y, 0, this.height)][MathHelper.clamp(x, 0, this.width)][7] = i;
    }

    public int[][][] getMap() {
        return this.map;
    }

    public boolean isRegional() {
        return this.regional;
    }

    public boolean isNorth() {
        return this.north;
    }

    public boolean isBuilt() {
        return this.built;
    }

    public boolean isLand(Point point) {
        return this.getY(point) > 127;
    }
    public boolean isLand(int x, int y) {
        return this.getY(x, y) > 127;
    }

    public void draw() {
        this.simplexTerrain();
        this.buildMountains();
        this.built = true;
//        this.findCoastlines();
//        this.feelTemperature();
//        this.aerate();
//        this.humidity();
    }

    private void simplexTerrain() {
        this.completion = 0f;
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                float pos = 0;
                float neg = 0;
                float z = 0;
                for (int n = 0; n < VisiwaConfig.HEIGHTMAP_OCTAVES; n++) {
                    pos += (this.simplex.sample(x/128.0/this.scaleFactor *(Math.pow(2, n)), y/128.0/this.scaleFactor *(Math.pow(2, n)))+.5)/(2*Math.pow(2, n));
                    neg += (this.simplex.sample(x/32.0/this.scaleFactor *(Math.pow(2, n)), y/32.0/this.scaleFactor *(Math.pow(2, n)))-1)/(2*Math.pow(2, n));
                    z = MathHelper.clamp((pos+neg)/2.0f+(VisiwaConfig.THIRSTINESS), -1.0f, 1.0f);
                }
                z /= 2;
                z += .5f;
                z *= 255;
                this.setY(AtlasHelper.clean(z), x, y);
                this.setC(AtlasHelper.clean(z), x, y);
            }
        }
    }

    private void buildMountains() {
        ArrayList<Point> rangePts = new ArrayList<>();
        ArrayList<Point> land = this.getLand();
        if (land.size() == 0) {
            return;
        }
        for (int x = 0; x < this.random.nextGaussian() * VisiwaConfig.SIGMA + 10 + VisiwaConfig.RANGINESS * this.width / 128f; x++) {
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
            int h4 = this.getY(ref);
            double h = (h1*VisiwaConfig.ROUNDNESS + h2*VisiwaConfig.SHARPNESS + h3*VisiwaConfig.BUBBLINESS + h4*VisiwaConfig.PYRAMIDNESS)
                    / ((VisiwaConfig.BUBBLINESS + VisiwaConfig.ROUNDNESS + VisiwaConfig.SHARPNESS + VisiwaConfig.PYRAMIDNESS));
            h /= 2.0;
            h += 127;
            this.setY(AtlasHelper.clean(h), point);
        }

    }

    private void findCoastlines() {
        double n = Math.pow(VisiwaConfig.COASTALNESS*2.0+1.0, 2);
        for (Point point : this.getLand()) {
            ArrayList<Point> neighbors = pointsWithinRange(point, VisiwaConfig.COASTALNESS);
            int oceanPoints = (int) neighbors.stream().filter(p1 -> this.getY(p1) < 128).count();
            int v = AtlasHelper.clean(255 * (((n-oceanPoints)/n)/2.0 + 0.5));
            this.setC(v, point);
            this.setH(255-v, point);
        }
    }

    private void feelTemperature() {
        if (!this.regional) {
            if (this.north) {
                for (int y = 0; y < this.height; y++) {
                    for (int x = 0; x < this.width; x++) {
                        int g = 0xFF - y;
                        int h = (int) (31*this.simplex.sample(x/255.0, y/255.0));
                        this.setT(AtlasHelper.clean((g + h) / 1.125), x, y);
                    }
                }
            } else {
                for (int y = 0; y < this.height; y++) {
                    for (int x = 0; x < this.width; x++) {
                        int h = (int) (31*this.simplex.sample(x/255.0, y/255.0));
                        this.setT(AtlasHelper.clean((y + h) / 1.125), x, y);
                    }
                }
            }


        }
        for (Point point : this.getLand()) {
            this.setT(AtlasHelper.clean(this.getT(point) - Math.pow((this.getY(point) - this.getC(point))/10.0, 2)), point);
        }
        for (int y = this.height - 2; y > 0; y--) {
            for (int x = 1; x < this.width - 1; x++) {
                this.setT(AtlasHelper.clean(
                        (this.getT(x, y) +
                        this.getT(x-1, y-1) +
                        this.getT(x, y-1) +
                        this.getT(x+1, y-1) +
                        this.getT(x-1, y) +
                        this.getT(x+1, y) +
                        this.getT(x-1, y+1) +
                        this.getT(x, y+1) +
                        this.getT(x+1, y+1)) / 9.0), x, y);
            }
        }
    }

    private void aerate() {
//        TODO: fix this (low airflow on continents, high everywhere else)
        for (int y = this.height - 2; y > 0; y--) {
            for (int x = 1; x < this.width - 1; x++) {
                // since the simplex sampler returns the same value for the same coordinate given the same seed, this
                // has the effect of approximately mapping low airflow areas to islands
                this.setA(this.getA(x, y) - AtlasHelper.clean(255 * (simplex.sample(x / 64.0, y / 64.0) + 0.5) / 2.0), x, y);
            }
        }
        if (!this.regional) {
            for (int y = this.height - 2; y > 0; y--) {
                for (int x = 1; x < this.width - 1; x++) {
                    if (this.isLand(x, y)) {
                        this.setA(this.getA(x, y) - AtlasHelper.clean(this.getA(x, y+1)*(((float) this.getY(x, y))/this.getY(x, y+1))+this.getC(x, y)/4.0), x, y);
                    }
                }
            }
            for (int y = this.height - 2; y > 0; y--) {
                for (int x = 1; x < this.width - 1; x++) {
                    this.setA(AtlasHelper.clean((this.getA(x,y) +
                            this.getA(x-1, y-1) +
                            this.getA(x, y-1) +
                            this.getA(x+1, y-1) +
                            this.getA(x-1, y) +
                            this.getA(x+1, y) +
                            this.getA(x-1, y+1) +
                            this.getA(x, y+1) +
                            this.getA(x+1, y+1)) / 9.0), x, y);
                }
            }
        }
    }

    private void humidity() {

    }

    public void printOut(AtlasHelper.PARAM p, String path) throws IOException {
        if (this.built) {
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int v;
                    switch (p) {
                        case CONTINENTALNESS -> v = (this.getC(x, y) & 0xFF);
                        case TEMPERATURE -> v = (this.getT(x, y) & 0xFF);
                        case AIRFLOW -> v = (this.getA(x, y) & 0xFF);
                        case PEAKS -> v = (this.getPV(x, y) & 0xFF);
                        case EROSION -> v = (this.getE(x, y) & 0xFF);
                        case HUMIDITY -> v = (this.getH(x, y) & 0xFF);
                        case WEIRDNESS -> v = (this.getW(x, y) & 0xFF);
                        default -> v = (this.getY(x, y) & 0xFF);
                    }
                    float h = v/255.0f;
                    int c = AtlasHelper.lerpColor((h-0.5f)*2, p);
                    img.setRGB(x, y, c);
                }
            }
            ImageIO.write(img, "png", new File(path + ".png"));
            System.out.println("Image generated");
        }
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
    public ArrayList<Point> pointsWithinRange(Point point, int radius)  {
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
                if (this.isLand(x, y)) {
                    land.add(new Point(x, y));
                }
            }
        }
        return land;
    }
    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.LONG.fieldOf("seed")
                                .stable()
                                .forGetter(Atlas::getSeed),
                        Codec.INT.fieldOf("scale_factor")
                                .stable()
                                .forGetter(Atlas::getScaleFactor),
                        Codec.BOOL.fieldOf("regional")
                                .stable()
                                .forGetter(Atlas::isRegional),
                        Codec.INT.fieldOf("width")
                                .stable()
                                .forGetter(Atlas::getWidth),
                        Codec.INT.fieldOf("height")
                                .stable()
                                .forGetter(Atlas::getHeight),
                        Codec.BOOL.fieldOf("north")
                                .stable()
                                .forGetter(Atlas::isNorth),
                        Codec.INT_STREAM.fieldOf("map")
                                .stable()
                                .forGetter(Atlas::getMapStream))
                        .apply(instance, instance.stable(Atlas::new)));
    }
}

