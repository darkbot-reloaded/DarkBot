package com.github.manolo8.darkbot.gui.trail;

import com.github.manolo8.darkbot.core.utils.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Path {

    public final List<Line> lines = new ArrayList<>();
    public final List<Location> points = new ArrayList<>();

    public static List<Path> of(Collection<Line> lines) {
        List<Path> paths = new ArrayList<>();

        Path current = null;
        Line last = null;

        for (Line line : lines) {
            if (last == null || last.x2 != line.x1 || last.y2 != line.y1) {
                paths.add(current = new Path());
                current.points.add(new Location(line.x1, line.y1));
            }
            current.lines.add(last = line);
            current.points.add(new Location(line.x2, line.y2));
        }
        return paths;
    }
}
