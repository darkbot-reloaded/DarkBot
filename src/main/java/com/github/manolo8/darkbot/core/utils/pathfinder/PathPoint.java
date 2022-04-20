package com.github.manolo8.darkbot.core.utils.pathfinder;

import eu.darkbot.api.game.other.Locatable;

import java.util.HashSet;
import java.util.Set;

public class PathPoint implements Locatable {

    public final double x;
    public final double y;

    public int f, g, s;
    public Set<PathPoint> lineOfSight = new HashSet<>();

    public PathPoint(Locatable loc) {
        this.x = loc.getX();
        this.y = loc.getY();
    }

    protected void fillLineOfSight(PathFinder finder) {
        lineOfSight.clear();

        for (PathPoint point : finder.getPathPoints())
            if (point != this && finder.hasLineOfSight(point, this))
                lineOfSight.add(point);
    }

    public Locatable asLocatable() {
        return Locatable.of(x, y);
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
