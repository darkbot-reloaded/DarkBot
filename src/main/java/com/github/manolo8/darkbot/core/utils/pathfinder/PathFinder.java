package com.github.manolo8.darkbot.core.utils.pathfinder;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.manager.MapManager;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Location;

import java.util.Comparator;
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

    public LinkedList<Locatable> createRote(Locatable current, Locatable destination) {
        Locatable fixedCurrent = fixToClosest(current);
        Locatable fixedDestination = fixToClosest(destination);

        LinkedList<Locatable> list = new LinkedList<>();

        // Always add an initial point if current needs to be fixed
        if (current.distanceTo(fixedCurrent) > 5) list.add(fixedCurrent);

        // Trivial case, just directly move to destination
        if (hasLineOfSight(fixedCurrent, fixedDestination)) {
            list.add(Locatable.of(fixedDestination.getX(), fixedDestination.getY()));
            return list;
        }

        // If no possible path is found, try to fly straight
        if (!PathFinderCalculator.calculate(this, fixedCurrent, fixedDestination, list))
            list.add(fixedDestination);

        return list;
    }

    public Locatable fixToClosest(final Locatable initial) {
        Location result = Location.of(initial.getX(), initial.getY());

        AreaImpl area = areaTo(result);
        if (area != null) {
            result.setTo(area.toSide(result)); // Inside an area, get out of it
            area = areaTo(result); // See if leaving an area got us inside another one
        }
        if (isOutOfMap(result)) { // In radiation, get out of it
            result.setTo(
                    Math.min(Math.max(result.getX(), 0), MapManager.internalWidth),
                    Math.min(Math.max(result.getY(), 0), MapManager.internalHeight));
            if (canMove(result)) return result; // Got out of rad and not in area
        } else if (area == null) return result; // Inside map & not in area (anymore)

        // Search for point in spiral pattern
        double angle = 0, distance = 0;
        while (distance < 20_000) {
            result.setTo(
                    initial.getX() - (cos(angle) * distance),
                    initial.getY() - (sin(angle) * distance));
            angle += 0.306;
            distance += 5;

            if (!isOutOfMap(result)
                    && (area == null || !area.containsPoint(result))
                    && (area = areaTo(result)) == null) {
                return result;
            }
        }

        // Worst case scenario, just pick the closest known path point
        if (distance >= 20000) {
            PathPoint closest = closest(initial);
            if (closest != null) return closest.toLocation();
        }
        return initial;
    }

    private PathPoint closest(Locatable point) {
        return points.stream().min(Comparator.comparingDouble(p -> p.distanceTo(point))).orElse(null);
    }

    public boolean isOutOfMap(Locatable loc) {
        return map.isOutOfMap(loc.getX(), loc.getY());
    }

    public boolean isOutOfMap(double x, double y) {
        return map.isOutOfMap(x, y);
    }

    public boolean canMove(Locatable loc) {
        return canMove(loc.getX(), loc.getY());
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

    private AreaImpl areaTo(Locatable point) {
        return obstacleHandler.stream().filter(a -> a.containsPoint(point.getX(), point.getY())).findAny().orElse(null);
    }

    boolean hasLineOfSight(Locatable point1, Locatable point2) {
        return obstacleHandler.stream().noneMatch(a -> a.intersectsLine(point1, point2));
    }

}
