package com.github.manolo8.darkbot.core.utils.pathfinder;

import com.github.manolo8.darkbot.core.utils.Location;
import eu.darkbot.api.game.other.Locatable;

import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class PathPoint implements Locatable {

    public final double x;
    public final double y;

    public int f;
    public int g;
    public int s;

    public Set<PathPoint> lineOfSight = new HashSet<>();

    public PathPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void fillLineOfSight(PathFinder finder) {
        lineOfSight.clear();

        for (PathPoint point : finder.points)
            if (point != this && finder.hasLineOfSight(point, this))
                lineOfSight.add(point);
    }

    public Location toLocation() {
        return new Location(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PathPoint pathPoint = (PathPoint) o;

        if (Double.compare(pathPoint.x, x) != 0) return false;
        return Double.compare(pathPoint.y, y) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(x);
        int result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }
}
