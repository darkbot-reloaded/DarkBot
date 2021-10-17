package com.github.manolo8.darkbot.core.utils.pathfinder;

import com.github.manolo8.darkbot.core.utils.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.min;

public class Rectangle extends Area {

    public double minX;
    public double minY;
    public double maxX;
    public double maxY;

    public boolean changed;

    public Rectangle(double minX, double minY, double maxX, double maxY) {
        this.minX = Math.floor(minX);
        this.minY = Math.floor(minY);
        this.maxX = Math.ceil(maxX);
        this.maxY = Math.ceil(maxY);
    }
    public static Rectangle ofSize(double minX, double minY, double width, double height) {
        return new Rectangle(minX, minY, minX + width, minY + height);
    }

    public boolean hasLineOfSight(PathPoint current, PathPoint destination) {
        return !collisionPoint(current.x, current.y, destination.x, destination.y);
    }

    private boolean collisionPoint(double x1, double y1, double x2, double y2) {
        if (x1 < x2 && lineCollisionLocation(x1, y1, x2, y2, minX, minY, minX, maxY))
            return true;
        else if (x1 > x2 && lineCollisionLocation(x1, y1, x2, y2, maxX, minY, maxX, maxY))
            return true;
        else if (y1 < y2 && lineCollisionLocation(x1, y1, x2, y2, minX, minY, maxX, minY))
            return true;
        else return y1 > y2 && lineCollisionLocation(x1, y1, x2, y2, minX, maxY, maxX, maxY);
    }

    public PathPoint toSide(PathPoint point) {

        int diffLeft = point.x - (int) minX;
        int diffRight = (int) maxX - point.x;

        int diffTop = point.y - (int) minY;
        int diffBottom = (int) maxY - point.y;

        int min = min(diffBottom, min(min(diffTop, diffLeft), diffRight));

        if (min == diffTop)
            point.y = (int) minY - 5;
        else if (min == diffBottom)
            point.y = (int) maxY + 5;
        else if (min == diffLeft)
            point.x = (int) minX - 5;
        else
            point.x = (int) maxX + 5;
        return point;
    }

    private boolean lineCollisionLocation(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double v = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        double uA = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / v;
        double uB = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / v;

        return uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1;
    }

    public boolean inside(int x, int y) {
        return (minX <= x && x <= maxX &&
                minY <= y && y <= maxY);
    }

    public Collection<PathPoint> getPoints(PathFinder pf) {
        List<PathPoint> points = new ArrayList<>(4);

        for (Corner corner : Corner.values())
            if (corner.include(pf, this))
                points.add(corner.get(this));

        return points;
    }

    private enum Corner {
        TOP_LEFT(-5, -5), TOP_RIGHT(5, -5), BOTTOM_LEFT(-5, 5),BOTTOM_RIGHT(5, 5);
        final int xDiff, yDiff;

        Corner(int x, int y) {
            this.xDiff = x;
            this.yDiff = y;
        }

        private int getX(Rectangle r) {
            return (int) (xDiff < 0 ? r.minX : r.maxX);
        }

        private int getY(Rectangle r) {
            return (int) (yDiff < 0 ? r.minY : r.maxY);
        }

        private boolean include(PathFinder pf, Rectangle r) {
            return !pf.isOutOfMap(getX(r), getY(r))
                    && pf.canMove(getX(r) + xDiff, getY(r) - yDiff)
                    && pf.canMove(getX(r) - xDiff, getY(r) + yDiff)
                    && pf.canMove(getX(r) + xDiff, getY(r) + yDiff);
        }

        private PathPoint get(Rectangle r) {
            return new PathPoint(getX(r) + this.xDiff, getY(r) + this.yDiff);
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

}
