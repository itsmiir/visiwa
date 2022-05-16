/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.world.gen;

import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;

public abstract class AtlasHelper {
    public static double getAngle(Point pt0, Point pt1) {
        if (pt0.equals(pt1)) {
            return 0;
        }
        double opp = pt1.y - pt0.y;
        double adj = pt1.x - pt0.x;
        double a;
        try {
            a = Math.atan(Math.abs(opp)/Math.abs(adj));
        } catch (ArithmeticException e) {
            if (opp < 0) {
                return 3*Math.PI/2d;
            } else {
                return Math.PI/2d;
            }
        }
        if (opp <= 0) {
            a += 2.0*Math.PI - 2.0*a;
        }
        if (adj <= 0) {
            a += Math.PI - 2*a;
        }
        return a;
    }

    public static class AtlasPixel {
        public int y; // elevation
        public int c; // continentalness
        public int t; // temperature
        public int a; // airflow
        public int h; // humidity
        public int e; // erosion
        public int pv;// peaks/valleys
        public int w; // weirdness
        public AtlasPixel() {
        }
    }


    public static Point[] findClosestPoint(Point point, ArrayList<Point> points) {
        return findClosestPoint(point, points, 1);
    }
    public static Point[] findClosestPoint(Point point, ArrayList<Point> points, int n) {
//        i am aware that this can be implemented without recursion. fuck you, recursion is cool
        Point[] closests = new Point[n];
        ArrayList<Point> searchPoints = new ArrayList<>(points);
        double minDist = Integer.MAX_VALUE;
        Point minPoint = point;
        if (n > 1) {
            Point[] pn = findClosestPoint(point, searchPoints, n - 1);
            int i = 0;
            for (Point p : pn) {
                closests[i] = p;
                i++;
                searchPoints.remove(p);
            }
        }
        for (Point p : searchPoints) {
            double d = p.distance(point);
            if (d < minDist) {
                if (p.equals(point)) {
                    continue;
                }
                minPoint = p;
                minDist = d;
            }
        }
        closests[n-1] = minPoint;
        return closests;
    }


    public static Color colorMap(float h, PARAM p) {
        int r, g, b;
        switch (p) {
            case ELEVATION:
                if (h < -0.5f) {
                    h += 0.5f;
                    h *= 2f;
                    r = clean(18*(-h) + 26*(1+h));
                    g = clean(9*(-h) + 56*(1+h));
                    b = clean(160*(-h) + 167*(1+h));
                } else if (h <= 0) {
                    h *= 2;
                    r = clean(26*(-h) + 34*(1+h));
                    g = clean(56*(-h) + 106*(1+h));
                    b = clean(167*(-h) + 138*(1+h));
                } else if (h < 0.5f) {
                    h *= 2;
                    r = clean(27*(1-h) + 155*(h));
                    g = clean(124*(1-h) + 143*(h));
                    b = clean(59*(1-h) + 78*(h));
                } else {
                    h -= 0.5;
                    h *= 2;
                    r = clean(155*(1-h) + 84*(h));
                    g = clean(143*(1-h) + 72*(h));
                    b = clean(78*(1-h) + 48*(h));
                }
                break;
            case CONTINENTALNESS:
                if (h < -0.5f) {
                    h += 0.5f;
                    h *= 2f;
                    r = clean(18*(-h) + 26*(1+h));
                    g = clean(9*(-h) + 56*(1+h));
                    b = clean(160*(-h) + 167*(1+h));
                } else if (h <= 0) {
                    h *= 2;
                    r = clean(26 * (-h) + 34 * (1 + h));
                    g = clean(56 * (-h) + 106 * (1 + h));
                    b = clean(167 * (-h) + 138 * (1 + h));
                } else {
                    r = clean(27*(1-h) + 200*(h));
                    g = clean(124*(1-h) + 100*(h));
                    b = clean(59*(1-h) + 0*(h));
                }
                break;
            default:
                r = 0;
                g = 0;
                b = 0;
        }

        return new Color(r, g, b);
    }

    public static int clean(double x) {
        return clean(Math.round(x));
    }
    public static int clean(float x) {
        return clean((Math.round(x)));
    }
    public static int clean(int x) {
        return MathHelper.clamp(x, 0, 255);
    }

    public enum PARAM {
        ELEVATION,
        CONTINENTALNESS,
        TEMPERATURE,
        AIRFLOW,
        HUMIDITY,
        EROSION,
        PEAKS,
        WEIRDNESS
    }
}
