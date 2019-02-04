package com.github.manolo8.darkbot.core.utils;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.StrictMath.atan2;

public class Location {

    public double x;
    public double y;

    public Location() {
    }

    public Location(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distance(double ox, double oy) {
        return sqrt(pow(x - ox, 2) + pow(y - oy, 2));
    }

    public double distance(Location o) {
        return sqrt(pow(x - o.x, 2) + pow(y - o.y, 2));
    }

    public double angle(Location o) {
        return atan2(y - o.y, x - o.x);
    }

    public Location copy() {
        return new Location(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Location) {
            Location location = (Location) obj;

            return location.x == x && location.y == y;
        }

        return false;
    }
}
