package com.github.manolo8.darkbot.gui.trail;

import com.github.manolo8.darkbot.utils.data.SizedIterable;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A method to smooth a hand-drawn line based on the McMaster
 * line smoothing algorithm.
 * <br>
 * Heavily modified version, based on the implementation by:
 * @author Derek Springer
 * @link <a href="https://12inchpianist.com/2010/07/30/line-smoothing-in-java/">...</a>
 */
public class Line {
    private long time;
    private final Location from, to;
    private final Location fromCopy, toCopy;

    public Line() {
        this.from = Location.of(0, 0);
        this.to = Location.of(0, 0);
        this.fromCopy = Location.of(0, 0);
        this.toCopy = Location.of(0, 0);
    }

    public void init(Locatable from, Locatable to) {
        this.time = System.currentTimeMillis();
        this.from.setTo(from);
        this.to.setTo(to);
    }

    public long getTime() {
        return time;
    }

    private Location getFrom() {
        return fromCopy.setTo(from.getX(), from.getY());
    }

    private Location getTo() {
        return toCopy.setTo(to.getX(), to.getY());
    }

    public static List<List<Location>> getSmoothedPaths(SizedIterable<Line> lines) {
        return getPaths(lines).stream().map(Line::smoothPath).collect(Collectors.toList());
    }

    /**
     * Split a single list of lines in N paths
     */
    private static List<List<Location>> getPaths(SizedIterable<Line> lines) {
        List<List<Location>> paths = new ArrayList<>();

        List<Location> current = null;
        Line last = null;

        for (Line line : lines) {
            if (last == null || !last.to.equals(line.from)) {
                paths.add(current = new ArrayList<>(lines.size() + 1));
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
