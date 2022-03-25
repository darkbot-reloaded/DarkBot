package com.github.manolo8.darkbot.core.utils.pathfinder;

import eu.darkbot.api.game.other.Locatable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PathFinderCalculator {

    private final PathFinder pf;
    private final PathPoint from;
    private final PathPoint to;

    private final Set<PathPoint> closedList;
    private final Set<PathPoint> openList;

    private final List<PathPoint> fragmentedPath;

    private PathFinderCalculator(PathFinder pf, Locatable from, Locatable to) {
        this.pf = pf;
        this.from =  new PathPoint(from.getX(), from.getY());
        this.to = new PathPoint(to.getX(), to.getY());

        this.fragmentedPath = new ArrayList<>();
        this.closedList = new HashSet<>();
        this.openList = new HashSet<>();
    }

    public static LinkedList<Locatable> calculate(
            PathFinder finder, Locatable from, Locatable to, LinkedList<Locatable> path) {
        if (path == null) path = new LinkedList<>();
        new PathFinderCalculator(finder, from, to).fillGeneratedPathTo(path);
        return path;
    }

    public void fillGeneratedPathTo(LinkedList<Locatable> target) {
        addPoints();

        if (build()) unfragment(target);

        removePoints();
    }

    private void addPoints() {
        pf.points.add(from);
        pf.points.add(to);

        from.fillLineOfSight(pf);
        for (PathPoint other : from.lineOfSight) other.lineOfSight.add(from);

        to.fillLineOfSight(pf);
        for (PathPoint other : to.lineOfSight) other.lineOfSight.add(to);
    }

    private void removePoints() {
        pf.points.remove(from);
        pf.points.remove(to);

        for (PathPoint other : to.lineOfSight) other.lineOfSight.remove(to);
        for (PathPoint other : from.lineOfSight) other.lineOfSight.remove(from);
    }

    private boolean build() {
        PathPoint current = from;

        current.f = (int) to.distanceTo(from);
        current.g = 0;
        current.s = 0;

        openList.add(current);

        fragmentedPath.add(from);

        do {

            openList.remove(current);
            closedList.add(current);

            update(current);
        } while ((current = pickupOne()) != to && !openList.isEmpty());
        return current == to;
    }

    private void update(PathPoint current) {
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

    private void unfragment(LinkedList<Locatable> target) {
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
