package com.github.manolo8.darkbot.core.utils.pathfinder;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.api.game.other.Locatable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PathFinderCalculator {

    private static final PathFinderCalculator INSTANCE = new PathFinderCalculator();

    private final Set<PathPoint> closedList = new HashSet<>();
    private final Set<PathPoint> openList = new HashSet<>();
    private final List<PathPoint> fragmentedPath = new ArrayList<>();

    private PathFinderCalculator() {}

    public static boolean calculate(PathFinder finder, Locatable from, Locatable to, LinkedList<Locatable> path) {
        synchronized (Main.UPDATE_LOCKER) {
            boolean isFromNew = finder.insertPathPoint(from);
            boolean isToNew = finder.insertPathPoint(to);

            PathPoint fromPoint = finder.getPathPoint(from);
            PathPoint toPoint = finder.getPathPoint(to);

            boolean foundPath = INSTANCE.build(fromPoint, toPoint);
            if (foundPath) INSTANCE.unfragment(fromPoint, toPoint, path);

            if (isFromNew) finder.removePathPoint(from);
            if (isToNew) finder.removePathPoint(to);
            return foundPath;
        }
    }

    private boolean build(PathPoint from, PathPoint to) {
        closedList.clear();
        openList.clear();
        fragmentedPath.clear();

        PathPoint current = from;
        current.f = (int) current.distanceTo(to);
        current.g = 0;
        current.s = 0;

        openList.add(current);
        fragmentedPath.add(current);
        do {
            openList.remove(current);
            closedList.add(current);
            update(current, to);
        } while ((current = pickupOne()) != to && !openList.isEmpty());
        return current == to;
    }

    private void update(PathPoint current, PathPoint to) {
        for (PathPoint neighbor : current.lineOfSight) {

            if (closedList.contains(neighbor))
                continue;

            int g = current.g + (int) current.distanceTo(neighbor);

            if (!openList.add(neighbor) && g >= neighbor.g)
                continue;

            neighbor.g = g;
            neighbor.s = current.s + 1;
            neighbor.f = g + (int) to.distanceTo(neighbor);

            fragmentedPath.add(neighbor);
        }
    }

    private void unfragment(PathPoint from, PathPoint to, LinkedList<Locatable> target) {
        PathPoint current = to;

        do {
            target.addFirst(current);
        } while ((current = next(current)) != from);
    }

    private PathPoint next(PathPoint current) {

        PathPoint closest = null;
        int sum = 0;

        for (PathPoint loop : fragmentedPath) {

            if (!current.lineOfSight.contains(loop)) continue;

            int csum = loop.g + (int) loop.distanceTo(current);

            if (current.s == loop.s + 1 && (closest == null || csum < sum)) {
                closest = loop;
                sum = csum;
            }

        }

        return closest;
    }

    private PathPoint pickupOne() {
        PathPoint better = null;

        for (PathPoint loop : openList) {
            if (better == null || loop.f < better.f) {
                better = loop;
            }
        }

        return better;
    }
}
