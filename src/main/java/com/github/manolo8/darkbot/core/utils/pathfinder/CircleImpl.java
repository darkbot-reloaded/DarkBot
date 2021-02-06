package com.github.manolo8.darkbot.core.utils.pathfinder;

import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.utils.MathUtils;
import eu.darkbot.api.entities.utils.Area;
import eu.darkbot.api.objects.Locatable;
import eu.darkbot.api.utils.PathFinder;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class CircleImpl extends AreaImpl implements Area.Circle {

    private static final double MARGIN = 25, POINT_DISTANCE = 100;

    private double x, y, radius, radiusSq;

    public CircleImpl(double x, double y, double radius) {
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
    public PathPoint toSide(Locatable point) {
        double angle = Math.atan2(y - point.getY(), x - point.getX());

        return new PathPoint((x - Math.cos(angle) * radius), (y - Math.sin(angle) * radius));
    }

    @Override
    public Collection<PathPoint> getPoints(@NotNull PathFinder pf) {
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

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public boolean containsPoint(double x, double y) {
        x -= this.x;
        y -= this.y;
        return x * x + y * y < radiusSq;
    }

    @Override
    public Rectangle getBounds() {
        return RectangleImpl.ofSize(x - radius,y - radius, x + radius, y + radius);
    }

    @Override
    public boolean intersectsLine(double x, double y, double x2, double y2) {
        return Line2D.ptSegDistSq(x, y, x2, y2, this.x, this.y) <= radiusSq;
    }

}
