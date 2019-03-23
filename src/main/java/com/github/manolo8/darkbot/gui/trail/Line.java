package com.github.manolo8.darkbot.gui.trail;

import com.github.manolo8.darkbot.core.utils.Location;

import java.awt.Point;

/**
 * A class to represent a line created from two points
 * @author Derek Springer
 */
public class Line {

    public double x1;
    public double y1;
    public double x2;
    public double y2;

    public Line(Location loc1, Location loc2) {
        this.x1 = loc1.x;
        this.y1 = loc1.y;
        this.x2 = loc2.x;
        this.y2 = loc2.y;
    }

    public double slope() {
        if(x2 - x1 == 0) return Double.NaN;
        return (y2 - y1) / (x2 - x1);
    }

    public double intercept() {
        return y1 - slope() * x1;
    }

    public static double slope(double x1, double y1, double x2, double y2) {
        return (y2-y1)/(x2-x1);
    }

    public static double slope(Point point1, Point point2) {
        return slope(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    public static double intercept(double x1, double y1, double x2, double y2) {
        return y1 - slope(x1, y1, x2, y2) * x1;
    }

    public static double intercept(Point point1, Point point2) {
        return intercept(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    @Override
    public String toString() {
        return "[(" + x1 + ", " + x2 + "), (" + y1 + ", " + y2 + ")] " +
                "m=" + slope() + ", b=" + intercept();
    }
}
