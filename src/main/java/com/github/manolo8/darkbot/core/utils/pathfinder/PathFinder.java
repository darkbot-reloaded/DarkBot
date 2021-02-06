package com.github.manolo8.darkbot.core.utils.pathfinder;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.utils.Location;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class PathFinder implements eu.darkbot.api.utils.PathFinder {

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

        // If no possible path is found, try to fly straight
        if (paths.isEmpty()) paths.add(destination);

        return paths;
    }

    public PathPoint fixToClosest(PathPoint point) {
        double initialX = point.x, initialY = point.y;

        AreaImpl area = areaTo(point);
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

    public boolean isOutOfMap(double x, double y) {
        return map.isOutOfMap(x, y);
    }

    public boolean canMove(double x, double y) {
        return obstacleHandler.stream().noneMatch(a -> a.containsPoint(x, y));
    }

    public boolean changed() {
        if (!obstacleHandler.changed()) return false;
        synchronized (Main.UPDATE_LOCKER) {
            points.clear();

            for (AreaImpl a : obstacleHandler)
                for (PathPoint p : a.getPoints(this))
                    if (!isOutOfMap(p.x, p.y) && canMove(p.x, p.y))
                        this.points.add(p);
        }

        for (PathPoint point : points) point.fillLineOfSight(this);
        return true;
    }

    private AreaImpl areaTo(PathPoint point) {
        return obstacleHandler.stream().filter(a -> a.containsPoint(point.x, point.y)).findAny().orElse(null);
    }

    boolean hasLineOfSight(PathPoint point1, PathPoint point2) {
        return obstacleHandler.stream().noneMatch(a -> a.intersectsLine(point1, point2));
    }

}
