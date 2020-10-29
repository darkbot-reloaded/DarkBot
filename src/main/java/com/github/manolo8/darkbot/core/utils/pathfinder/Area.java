package com.github.manolo8.darkbot.core.utils.pathfinder;

import java.util.Collection;

public abstract class Area {

    public boolean changed;

    public abstract boolean hasLineOfSight(PathPoint current, PathPoint destination);

    public abstract PathPoint toSide(PathPoint point);

    public abstract boolean inside(int x, int y);

    public abstract Collection<PathPoint> getPoints(PathFinder pf);

}
