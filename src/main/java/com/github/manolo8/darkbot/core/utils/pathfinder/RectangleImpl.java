package com.github.manolo8.darkbot.core.utils.pathfinder;

import com.github.manolo8.darkbot.core.utils.Location;
import eu.darkbot.api.game.other.Area;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.utils.PathFinder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.min;

public class RectangleImpl extends AreaImpl implements Area.Rectangle {

    private static final int MARGIN = 5;
    
    public double minX;
    public double minY;
    public double maxX;
    public double maxY;

    public RectangleImpl() {
    }

    public RectangleImpl(double minX, double minY, double maxX, double maxY) {
        this.minX = Math.floor(minX);
        this.minY = Math.floor(minY);
        this.maxX = Math.ceil(maxX);
        this.maxY = Math.ceil(maxY);
    }

    public static RectangleImpl ofSize(double minX, double minY, double width, double height) {
        return new RectangleImpl(minX, minY, minX + width, minY + height);
    }

    public Locatable toSide(Locatable point) {
        double diffLeft = point.getX() - minX, diffRight = maxX - point.getX();
        double diffTop = point.getY() - minY, diffBottom = maxY - point.getY();

        double newX = point.getX(), newY = point.getY();

        double min = min(min(diffBottom, diffTop), min(diffLeft, diffRight));
        if (min == diffTop) newY = minY - MARGIN;
        else if (min == diffBottom) newY = maxY + MARGIN;
        else if (min == diffLeft) newX = minX - MARGIN;
        else newX = maxX + MARGIN;

        return Locatable.of(newX, newY);
    }

    public Collection<Locatable> getPoints(@NotNull eu.darkbot.api.utils.PathFinder pf) {
        List<Locatable> points = new ArrayList<>(4);

        for (Corner corner : Corner.values())
            if (corner.include(pf, this))
                points.add(corner.get(this));

        return points;
    }

    private enum Corner {
        TOP_LEFT(-MARGIN, -MARGIN),
        TOP_RIGHT(MARGIN, -MARGIN),
        BOTTOM_LEFT(-MARGIN, MARGIN),
        BOTTOM_RIGHT(MARGIN, MARGIN);

        private final int xDiff, yDiff;

        Corner(int x, int y) {
            this.xDiff = x;
            this.yDiff = y;
        }

        private int getX(Rectangle r) {
            return (int) (xDiff < 0 ? r.getX() : r.getX2());
        }

        private int getY(Rectangle r) {
            return (int) (yDiff < 0 ? r.getY() : r.getY2());
        }

        private boolean include(PathFinder pf, Rectangle r) {
            return !pf.isOutOfMap(getX(r), getY(r))
                    && pf.canMove(getX(r) + xDiff, getY(r) - yDiff)
                    && pf.canMove(getX(r) - xDiff, getY(r) + yDiff)
                    && pf.canMove(getX(r) + xDiff, getY(r) + yDiff);
        }

        private Locatable get(Rectangle r) {
            return Locatable.of(getX(r) + this.xDiff, getY(r) + this.yDiff);
        }
    }

    public void set(Location o, int addX, int addY) {
        this.maxX = o.x + addX;
        this.minX = o.x - addX;
        this.maxY = o.y + addY;
        this.minY = o.y - addY;

        changed = true;
    }

    public void set(double minX, double minY, double maxX, double maxY) {
        if (this.minX == minX && this.minY == minY && this.maxX == maxX && this.maxY == maxY) return;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;

        changed = true;
    }

    public double size() {
        return (maxX - minX) * (maxY - minY);
    }

    @Override
    public boolean isEmpty() {
        return size() <= 0;
    }

    @Override
    public double getX() {
        return minX;
    }

    @Override
    public double getY() {
        return minY;
    }

    @Override
    public double getX2() {
        return maxX;
    }

    @Override
    public double getY2() {
        return maxY;
    }

    @Override
    public boolean intersectsLine(double x1, double y1, double x2, double y2) {
        if (x1 < x2 && Area.linesIntersect(x1, y1, x2, y2, minX, minY, minX, maxY))
            return true;
        else if (x1 > x2 && Area.linesIntersect(x1, y1, x2, y2, maxX, minY, maxX, maxY))
            return true;
        else if (y1 < y2 && Area.linesIntersect(x1, y1, x2, y2, minX, minY, maxX, minY))
            return true;
        else
            return y1 > y2 && Area.linesIntersect(x1, y1, x2, y2, minX, maxY, maxX, maxY);
    }

}
