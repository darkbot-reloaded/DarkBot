package com.github.manolo8.darkbot.gui.trail;

import eu.darkbot.api.game.other.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A method to smooth a hand-drawn line based on the McMaster
 * line smoothing algorithm
 *
 * Heavily modified version, based on the implementation by:
 * @author Derek Springer
 * @link https://12inchpianist.com/2010/07/30/line-smoothing-in-java/
 */
public class Line {
    private final Location from, to;
    private Location fromCopy, toCopy;

    public Line(Location from, Location to) {
        this.from = from;
        this.to = to;
    }

    private Location getFrom() {
        return fromCopy == null ? fromCopy = from.copy() : fromCopy.setTo(from.getX(), from.getY());
    }

    private Location getTo() {
        return toCopy == null ? toCopy = to.copy() : toCopy.setTo(to.getX(), to.getY());
    }

    public static List<List<Location>> getSmoothedPaths(Collection<Line> lines) {
        return getPaths(lines).stream().map(Line::smoothPath).collect(Collectors.toList());
    }

    /**
     * Split a single list of lines in N paths
     */
    private static List<List<Location>> getPaths(Collection<Line> lines) {
        List<List<Location>> paths = new ArrayList<>();

        List<Location> current = null;
        Line last = null;

        for (Line line : lines) {
            if (last == null || last.to != line.from) {
                paths.add(current = new ArrayList<>());
                current.add(line.getFrom());
            }
            last = line;
            current.add(line.getTo());
        }
        return paths;
    }

    /**
     * @param path A list of line segments representing a line
     * @return A list line segments representing the smoothed line
     */
    private static List<Location> smoothPath(List<Location> path) {
        int size = path.size();
        if (size < 4) return path;

        Location[] points = new Location[5];
        for (int i = 0; i < 5; i++) points[i] = Location.of(0, 0);

        for (int i = 0; i <= size; i++) {
            if (i < size) {
                Location loc = path.get(i);
                points[i % points.length].setTo(loc.getX(), loc.getY());
            } else {
                points[i % points.length] = null;
                points[(i + 1) % points.length] = null;
            }
            if (i < 5) continue;

            smoothPoint(points, path.get(i - 2));
        }
        return path;
    }

    /**
     * Find the new point for a smoothed line segment
     * @param points The n points around the smoothed point
     */
    private static void smoothPoint(Location[] points, Location oldPoint) {
        double sumX = 0, sumY = 0, total = 0;
        for(Location point : points) {
            if (point == null) continue;
            sumX += point.getX();
            sumY += point.getY();
            total++;
        }

        oldPoint.setTo(((sumX / total) + oldPoint.getX()) / 2,
                ((sumY / total) + oldPoint.getY()) / 2);
    }
}
