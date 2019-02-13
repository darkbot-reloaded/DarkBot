package com.github.manolo8.darkbot.core.utils.pathfinder;

import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.utils.Location;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PathFinder {

    final LinkedList<PathPoint> paths;
    public final Set<PathPoint> points;

    final List<Obstacle> obstacles;

    private int lastSize;

    public PathFinder(List<Obstacle> obstacles) {
        this.paths = new LinkedList<>();
        this.obstacles = obstacles;
        this.points = new HashSet<>();
    }

    public Location current() {
        if (paths.size() > 0) {
            PathPoint point = paths.getFirst();

            return new Location(point.x, point.y);
        }
        return null;
    }

    public void currentCompleted() {
        paths.removeFirst();
    }

    public boolean isEmpty() {
        return paths.isEmpty();
    }

    public void createRote(Location current, Location destination) {
        createRote(
                new PathPoint((int) current.x, (int) current.y),
                new PathPoint((int) destination.x, (int) destination.y)
        );
    }

    private void createRote(PathPoint current, PathPoint destination) {
        if (hasLineOfSight(current, destination)) {

            paths.clear();
            paths.add(destination);

        } else {

            paths.clear();

            checkModification();

            fixToClosest(current);
            fixToClosest(destination);

            current.fillLineOfSight(this);
            destination.fillLineOfSight(this);

            new PathFinderCalculator(
                    current,
                    destination
            ).fillGeneratedPathTo(paths);
        }
    }

    //Need some improvements '-'
    private void fixToClosest(PathPoint point) {

        Area area = areaTo(point);

        if (area != null) {
            area.toSide(point);
        }

        area = areaTo(point);

        //Well, our method fails...
        if (area != null) {
            PathPoint closest = closest(point);

            point.x = closest.x;
            point.y = closest.y;
        }

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

    private void checkModification() {
        for (Obstacle obstacle : obstacles) {

            Area area = obstacle.getArea();

            if (area.changed || area.cachedUsing != obstacle.use()) {
                rebuild();
                return;
            }
        }

        if (obstacles.size() != lastSize) {
            rebuild();
        }
    }

    private void rebuild() {
        points.clear();
        lastSize = obstacles.size();

        rebuildPoints();
        rebuildLineOfSight();
    }

    private void rebuildPoints() {
        for (Obstacle obstacle : obstacles) {

            if (obstacle.use()) {

                Area a = obstacle.getArea();

                a.changed = false;

                //LEFT AND TOP
                checkAndAddPoint(new PathPoint((int) a.minX - 1, (int) a.minY - 1));
                //LEFT AND BOTTOM
                checkAndAddPoint(new PathPoint((int) a.minX - 1, (int) (a.maxY) + 1));
                //RIGHT AND TOP
                checkAndAddPoint(new PathPoint((int) (a.maxX) + 1, (int) a.minY - 1));
                //RIGHT AND BOTTOM
                checkAndAddPoint(new PathPoint((int) (a.maxX) + 1, (int) (a.maxY) + 1));
            }

        }
    }

    private void checkAndAddPoint(PathPoint point) {
        if (collisionCount(point) == 0) {
            points.add(point);
        }
    }

    private int collisionCount(PathPoint point) {

        int count = 0;

        for (Obstacle obstacle : obstacles) {
            if (obstacle.use()) {
                Area area = obstacle.getArea();
                if (area.inside(point.x, point.y))
                    count++;
            }
        }

        return count;
    }

    private Area areaTo(PathPoint point) {
        for (Obstacle obstacle : obstacles) {
            if (obstacle.use()) {
                Area area = obstacle.getArea();
                if (area.inside(point.x, point.y)) {
                    return area;
                }
            }
        }

        return null;
    }

    public boolean canMove(Location location) {
        for (Obstacle obstacle : obstacles) {
            if (obstacle.use()) {
                Area area = obstacle.getArea();
                if (area.inside((int) location.x, (int) location.y)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void rebuildLineOfSight() {
        for (PathPoint point : points) {
            point.fillLineOfSight(this);
        }
    }

    boolean hasLineOfSight(PathPoint point1, PathPoint point2) {

        for (Obstacle obstacle : obstacles) {

            if (obstacle.use()) {

                Area area = obstacle.getArea();

                if (!area.hasLineOfSight(point1, point2)) {
                    return false;
                }
            }

        }

        return true;
    }

    public List<PathPoint> path() {
        return paths;
    }
}
