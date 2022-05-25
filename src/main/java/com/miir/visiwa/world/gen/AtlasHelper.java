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


    public static int lerpColor(float h, PARAM p) {
        int r, g, b, rgb1, rgb2, rgb3, rgb4, rgb5, rgb6;
        switch (p) {
            case ELEVATION -> {
                rgb1 = 0x1209A0;
                rgb2 = 0x1A38A7;
                rgb3 = 0x226A8A;
                rgb4 = 0x1B7C3B;
                rgb5 = 0x9B8F4E;
                rgb6 = 0x544830;
            }
            case CONTINENTALNESS -> {
                rgb1 = 0x1209A0;
                rgb2 = 0x1A38A7;
                rgb3 = 0x226A8A;
                rgb4 = 0x1B7C3B;
                rgb5 = 0xC86400;
                rgb6 = 0xFF0000;
            }
            default -> {
                rgb1 = 0x9451B3;
                rgb2 = 0x6DC7C2;
                rgb3 = 0x58964C;
                rgb4 = 0x58964C;
                rgb5 = 0xC5AB4D;
                rgb6 = 0x814B50;
            }
        }
        if (h < -0.5f) {
            h += 0.5f;
            h *= 2f;
            r = clean((rgb1 >> 16 & 0xFF)*(-h) +   (rgb2 >> 16 & 0xFF)*(1+h));
            g = clean((rgb1 >> 8 & 0xFF)*(-h) +    (rgb2 >> 8 & 0xFF)*(1+h));
            b = clean((rgb1 & 0xFF)*(-h) +         (rgb2 & 0xFF)*(1+h));
        } else if (h <= 0) {
            h *= 2;
            r = clean((rgb2 >> 16 & 0xFF) * (-h) + (rgb3 >> 16 & 0xFF) * (1 + h));
            g = clean((rgb2 >> 8 & 0xFF) * (-h) +  (rgb3 >> 8 & 0xFF) * (1 + h));
            b = clean((rgb2 & 0xFF) * (-h) +       (rgb3 & 0xFF) * (1 + h));
        } else if (h <= 0.5) {
            h *= 2;
            r = clean( (rgb4 >> 16 & 0xFF)*(1-h) + (rgb5 >> 16 & 0xFF)*(h));
            g = clean((rgb4 >> 8 & 0xFF)*(1-h) +   (rgb5 >> 8 & 0xFF)*(h));
            b = clean((rgb4 & 0xFF)*(1-h) +        (rgb5 & 0xFF)*(h));
        } else {
            h -= 0.5;
            h *= 2;
            r = clean((rgb5 >> 16 & 0xFF)*(1-h) +  (rgb6 >> 16 & 0xFF)*(h));
            g = clean((rgb5 >> 8 & 0xFF)*(1-h) +   (rgb6 >> 8 & 0xFF)*(h));
            b = clean((rgb5 & 0xFF)*(1-h) +        (rgb6 & 0xFF)*(h));
        }
        return r << 16 | g << 8 | b;
    }

    public static int clean(double d) {
        return clean(Math.round(d));
    }
    public static int clean(float f) {
        return clean((Math.round(f)));
    }
    public static int clean(int i) {
        return MathHelper.clamp(i, 0, 255);
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
