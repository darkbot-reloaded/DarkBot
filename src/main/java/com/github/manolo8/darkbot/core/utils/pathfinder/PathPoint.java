package com.github.manolo8.darkbot.core.utils.pathfinder;

import com.github.manolo8.darkbot.core.utils.Location;

import java.util.HashSet;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class PathPoint {

    public int x;
    public int y;

    public int f;
    public int g;
    public int s;

    public HashSet<PathPoint> lineOfSight = new HashSet<>();

    public PathPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double distance(PathPoint o) {
        return sqrt(pow(x - o.x, 2) + pow(y - o.y, 2));
    }

    public void fillLineOfSight(PathFinder finder) {

        lineOfSight.clear();

        for (PathPoint point : finder.points)
            if (point != this)
                if (finder.hasLineOfSight(point, this))
                    lineOfSight.add(point);
    }

    public Location toLocation() {
        return new Location(x, y);
    }

    @Override
    public int hashCode() {
        int var1 = 1664525 * this.x + 1013904223;
        int var2 = 1664525 * (this.y ^ -559038737) + 1013904223;
        return var1 ^ var2;
    }
}
