package com.github.manolo8.darkbot.gui.trail;

import com.github.manolo8.darkbot.core.utils.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A method to smooth a hand-drawn line based on the McMaster
 * line smoothing algorithm
 *
 * @author Derek Springer
 * @link https://12inchpianist.com/2010/07/30/line-smoothing-in-java/
 */
public class LineSmoother {

    public static List<List<Location>> getSmoothedPaths(Collection<Line> lines) {
        return Path.of(lines).stream().map(LineSmoother::smoothPath).collect(Collectors.toList());
    }

    /**
     * @param path A list of line segments representing a line
     * @return A list line segments representing the smoothed line
     */
    public static List<Location> smoothPath(Path path) {
        if(path.lines.size() < 4) return path.points;

        List<Location> smoothPoints = new ArrayList<>();
        smoothPoints.add(path.points.get(0));
        smoothPoints.add(path.points.get(1));

        for (int i = 2; i < path.points.size() - 2; i++)
            smoothPoints.add(smoothPoint(path.points.subList(i - 2, i + 3)));

        smoothPoints.add(smoothPoint(path.points.subList(path.points.size() - 3, path.points.size())));
        smoothPoints.add(path.points.get(path.points.size() - 1));
        return smoothPoints;
    }

    /**
     * Find the new point for a smoothed line segment
     * @param points The five points needed
     * @return The new point for the smoothed line segment
     */
    private static Location smoothPoint(List<Location> points) {
        int avgX = 0;
        int avgY = 0;
        for(Location point : points) {
            avgX += point.x;
            avgY += point.y;
        }

        avgX = avgX/points.size();
        avgY = avgY/points.size();
        Location newPoint = new Location(avgX, avgY);
        Location oldPoint = points.get(points.size()/2);
        double newX = (newPoint.x + oldPoint.x)/2;
        double newY = (newPoint.y + oldPoint.y)/2;

        return new Location(newX, newY);
    }
}