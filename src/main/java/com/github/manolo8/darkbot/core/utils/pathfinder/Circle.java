package com.github.manolo8.darkbot.core.utils.pathfinder;

import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.utils.MathUtils;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class Circle extends Area {

    private static final double MARGIN = 25, POINT_DISTANCE = 100;

    private double x, y, radius, radiusSq;

    public Circle(double x, double y, double radius) {
        set(x, y, radius);
    }

    public void set(Location loc, double radius) {
        set(loc.x, loc.y, radius);
    }

    public void set(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.radiusSq = radius * radius;
        this.changed = true;
    }

    @Override
    public boolean hasLineOfSight(PathPoint a, PathPoint b) {
        return Line2D.ptSegDistSq(a.x, a.y, b.x, b.y, x, y) > radiusSq;
    }

    @Override
    public PathPoint toSide(PathPoint point) {
        double angle = Math.atan2(y - point.y, x - point.x);

        point.x = (int) (x - Math.cos(angle) * radius);
        point.y = (int) (y - Math.sin(angle) * radius);

        return point;
    }

    @Override
    public boolean inside(int x, int y) {
        x -= this.x;
        y -= this.y;
        return x * x + y * y < radiusSq;
    }

    @Override
    public Collection<PathPoint> getPoints(PathFinder pf) {
        int pointCount = (int) (MathUtils.TAU * radius / POINT_DISTANCE);

        List<PathPoint> points = new ArrayList<>(pointCount);

        double angleDiff = MathUtils.TAU / pointCount;
        for (double angle = 0; angle < MathUtils.TAU; angle += angleDiff) {
            points.add(new PathPoint(
                    (int) (x - Math.cos(angle) * (radius + MARGIN)),
                    (int) (y - Math.sin(angle) * (radius + MARGIN))));
        }

        return points;
    }
}
