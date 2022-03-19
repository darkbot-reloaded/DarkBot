package com.github.manolo8.darkbot.core.utils.pathfinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PathFinderCalculator {

    private final PathPoint come;
    private final PathPoint destination;

    private final Set<PathPoint> closedList;
    private final Set<PathPoint> openList;

    private final List<PathPoint> fragmentedPath;

    private PathFinderCalculator(PathPoint from, PathPoint to) {
        this.come = from;
        this.destination = to;

        this.fragmentedPath = new ArrayList<>();
        this.closedList = new HashSet<>();
        this.openList = new HashSet<>();
    }

    public static LinkedList<PathPoint> calculate(PathPoint from, PathPoint to) {
        LinkedList<PathPoint> list = new LinkedList<>();
        new PathFinderCalculator(from, to).fillGeneratedPathTo(list);
        return list;
    }

    public void fillGeneratedPathTo(LinkedList<PathPoint> target) {
        addDefs();

        if (build()) unfragment(target);

        remDefs();
    }

    private void addDefs() {
        for (PathPoint temp : destination.lineOfSight)
            temp.lineOfSight.add(destination);

        for (PathPoint temp : come.lineOfSight)
            temp.lineOfSight.add(come);
    }

    private void remDefs() {
        for (PathPoint temp : destination.lineOfSight)
            temp.lineOfSight.remove(destination);

        for (PathPoint temp : come.lineOfSight)
            temp.lineOfSight.remove(come);
    }

    private boolean build() {

        PathPoint current = come;

        current.f = (int) destination.distanceTo(come);
        current.g = 0;
        current.s = 0;

        openList.add(current);

        fragmentedPath.add(come);

        do {

            openList.remove(current);
            closedList.add(current);

            update(current);
        } while ((current = pickupOne()) != destination && !openList.isEmpty());
        return current == destination;
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
            neighbor.f = g + (int) destination.distanceTo(neighbor);

            fragmentedPath.add(neighbor);
        }
    }

    private void unfragment(LinkedList<PathPoint> target) {
        PathPoint current = destination;

        do {
            target.addFirst(current);
        } while ((current = next(current)) != come);
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
