package com.github.manolo8.darkbot.core.utils.pathfinder;

import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.utils.Location;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class PathFinder {

    private final MapManager map;
    public final Set<PathPoint> points;

    private final ObstacleHandler obstacleHandler;

    public PathFinder(MapManager map) {
        this.map = map;
        this.obstacleHandler = new ObstacleHandler(map);
        this.points = new HashSet<>();
    }

    public LinkedList<PathPoint> createRote(Location current, Location destination) {
        return createRote(
                new PathPoint((int) current.x, (int) current.y),
                new PathPoint((int) destination.x, (int) destination.y)
        );
    }

    public LinkedList<PathPoint> createRote(PathPoint current, PathPoint destination) {
        LinkedList<PathPoint> paths = new LinkedList<>();
        fixToClosest(current);
        fixToClosest(destination);

        if (hasLineOfSight(current, destination)) {
            paths.add(destination);
            return paths;
        }

        current.fillLineOfSight(this);
        destination.fillLineOfSight(this);

        new PathFinderCalculator(current, destination)
                .fillGeneratedPathTo(paths);
        return paths;
    }

    public PathPoint fixToClosest(PathPoint point) {
        int initialX = point.x, initialY = point.y;

        Area area = areaTo(point);
        if (area != null) area = areaTo(area.toSide(point)); // Inside an area, get out of it
        if (map.isOutOfMap(point.x, point.y)) { // In radiation, get out of it
            point.x = Math.min(Math.max(point.x, 0), MapManager.internalWidth);
            point.y = Math.min(Math.max(point.y, 0), MapManager.internalHeight);
            if (areaTo(point) == null) return point; // Got out of rad and not in area
        } else if (area == null) return point; // Inside map & not in area (anymore)

        double angle = 0, distance = 0;
        do {
            point.x = initialX - (int) (cos(angle) * distance);
            point.y = initialY - (int) (sin(angle) * distance);
            angle += 0.3;
            distance += 2;
        } while (areaTo(point) != null || map.isOutOfMap(point.x, point.y) && distance < 20000);

        if (distance >= 20000) {
            PathPoint closest = closest(point);
            if (closest == null) return point;
            point.x = closest.x;
            point.y = closest.y;
        }
        return point;
    }

    private PathPoint closest(PathPoint point) {

        double distance = 0;
        PathPoint current = null;

        for (PathPoint loop : points) {
            double cd = loop.distance(point);

            if (current == null || cd < distance) {
                current = loop;
                distance = cd;
            }

        }

        return current;
    }

    public boolean changed() {
        if (!obstacleHandler.changed()) return false;
        points.clear();

        rebuildPoints();
        rebuildLineOfSight();
        return true;
    }

    private void rebuildPoints() {
        for (Area a : obstacleHandler) {
            checkAndAddPoint((int) a.minX, (int) a.minY, Corner.TOP_LEFT);
            checkAndAddPoint((int) a.maxX, (int) a.minY, Corner.TOP_RIGHT);
            checkAndAddPoint((int) a.minX, (int) a.maxY, Corner.BOTTOM_LEFT);
            checkAndAddPoint((int) a.maxX, (int) a.maxY, Corner.BOTTOM_RIGHT);
        }
    }

    private void checkAndAddPoint(int x, int y, Corner corner) {
        if (!map.isOutOfMap(x, y)
                && canMove(x + corner.x, y - corner.y)
                && canMove(x - corner.x, y + corner.y)
                && canMove(x + corner.x, y + corner.y))
            points.add(new PathPoint(x + corner.x, y + corner.y));
    }

    public boolean canMove(int x, int y) {
        return obstacleHandler.stream().noneMatch(a -> a.inside(x, y));
    }

    private Area areaTo(PathPoint point) {
        return obstacleHandler.stream().filter(a -> a.inside(point.x, point.y)).findAny().orElse(null);
    }

    private void rebuildLineOfSight() {
        for (PathPoint point : points) point.fillLineOfSight(this);
    }

    boolean hasLineOfSight(PathPoint point1, PathPoint point2) {
        return obstacleHandler.stream().allMatch(a -> a.hasLineOfSight(point1, point2));
    }

    private enum Corner {
        TOP_LEFT(-1, -1), TOP_RIGHT(1, -1), BOTTOM_LEFT(-1, 1),BOTTOM_RIGHT(1, 1);
        int x,y;

        Corner(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

}
