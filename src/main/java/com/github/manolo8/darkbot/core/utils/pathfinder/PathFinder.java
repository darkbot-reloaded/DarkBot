package com.github.manolo8.darkbot.core.utils.pathfinder;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.manager.MapManager;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Location;
import eu.darkbot.api.managers.ConfigAPI;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class PathFinder implements eu.darkbot.api.utils.PathFinder {

    private final MapManager map;
    private final Map<Locatable, PathPoint> points;

    private final ObstacleHandler obstacleHandler;
    private final RadiationHandler radiationHandler;

    public PathFinder(MapManager map, ConfigAPI configAPI) {
        this.map = map;
        this.obstacleHandler = new ObstacleHandler(map);
        this.radiationHandler = new RadiationHandler(configAPI);
        this.points = new HashMap<>();
    }

    public Collection<PathPoint> getPathPoints() {
        return Collections.unmodifiableCollection(points.values());
    }

    public PathPoint getPathPoint(Locatable point) {
        return points.get(point);
    }

    public LinkedList<Locatable> createRote(Locatable current, Locatable destination) {
        Locatable fixedCurrent = toImmutable(fixToClosest(current));
        Locatable fixedDestination = toImmutable(fixToClosest(destination));

        LinkedList<Locatable> list = new LinkedList<>();

        if (hasLineOfSight(fixedCurrent, fixedDestination)
                || !PathFinderCalculator.calculate(this, fixedCurrent, fixedDestination, list)) {
            // Either trivial case, or no path exists
            list.add(fixedDestination);
        }

        // Always add an initial point if current needs to be fixed
        if (current.distanceTo(fixedCurrent) > 5) list.addFirst(fixedCurrent);

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
            Locatable closest = closest(initial);
            if (closest != null) return closest;
        }
        return initial;
    }

    private Locatable toImmutable(Locatable point) {
        if (point instanceof Locatable.LocatableImpl) return point;
        return Locatable.of(point.getX(), point.getY());
    }

    public boolean changed() {
        if (!obstacleHandler.changed() & !radiationHandler.changed()) return false;
        synchronized (Main.UPDATE_LOCKER) {
            points.clear();

            for (AreaImpl a : obstacleHandler) {
                for (Locatable p : a.getPoints(this)) {
                    if (!isOutOfMap(p) && canMove(p))
                        // Ensure the locatable is immutable and properly implements equals & hashCode
                        this.points.put(toImmutable(p), new PathPoint(p));
                }
            }
            for (PathPoint point : points.values()) point.fillLineOfSight(this);
        }

        return true;
    }

    protected boolean insertPathPoint(Locatable point) {
        point = toImmutable(point);
        if (points.containsKey(point)) return false;

        PathPoint newPoint = new PathPoint(point);
        points.put(point, newPoint);

        newPoint.fillLineOfSight(this);
        for (PathPoint other : newPoint.lineOfSight) other.lineOfSight.add(newPoint);
        return true;
    }

    protected void removePathPoint(Locatable point) {
        point = toImmutable(point);

        PathPoint oldPoint = points.remove(point);
        if (oldPoint == null) return;
        for (PathPoint other : oldPoint.lineOfSight) other.lineOfSight.remove(oldPoint);
    }

    private Locatable closest(Locatable point) {
        synchronized (Main.UPDATE_LOCKER) {
            return points.keySet().stream().min(Comparator.comparingDouble(p -> p.distanceTo(point))).orElse(null);
        }
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
        for (AreaImpl area : obstacleHandler) {
            if (area.containsPoint(x, y))
                return false;
        }
        return true;
    }

    private AreaImpl areaTo(Locatable point) {
        for (AreaImpl area : obstacleHandler) {
            if (area.containsPoint(point.getX(), point.getY()))
                return area;
        }
        return null;
    }

    boolean hasLineOfSight(Locatable point1, Locatable point2) {
        for (AreaImpl area : obstacleHandler) {
            if (area.intersectsLine(point1, point2))
                return false;
        }
        return true;
    }
}
