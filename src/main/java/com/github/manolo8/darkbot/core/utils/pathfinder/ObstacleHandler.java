package com.github.manolo8.darkbot.core.utils.pathfinder;

import com.github.manolo8.darkbot.config.ZoneInfo;
import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.manager.MapManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ObstacleHandler implements Iterable<AreaImpl> {

    private final MapManager map;
    private final List<Obstacle> obstacles;
    private boolean[] used = new boolean[0];
    private ZoneInfo avoided;
    private List<AreaImpl> areas = new ArrayList<>();

    public ObstacleHandler(MapManager map) {
        this.map = map;
        this.obstacles = map.entities.obstacles;
        this.avoided = new ZoneInfo(1);
    }

    boolean changed() {
        boolean changed = checkChanged();
        if (changed) rebuildAreas();
        return changed;
    }

    public List<AreaImpl> get() {
        return areas;
    }

    public Stream<AreaImpl> stream() {
        return areas.stream();
    }

    /**
     * Checks for changes and updates used[*] state
     * @return true if any change happened, false otherwise
     */
    private boolean checkChanged() {
        boolean changed = obstacles.size() != used.length;
        if (changed) used = new boolean[obstacles.size()];

        changed |= map.avoided != avoided || avoided.changed;

        for (int i = 0; i < obstacles.size(); i++) {
            Obstacle ob = obstacles.get(i);
            boolean shouldUse = ob.use();
            changed |= (used[i] != shouldUse) || ob.getArea().changed;

            used[i] = shouldUse;
            ob.getArea().changed = false;
        }
        return changed;
    }

    private void rebuildAreas() {
        if (obstacles == null || map.avoided == null) return;
        avoided = map.avoided;

        double cellSize = 1d / avoided.resolution,
                width = cellSize * MapManager.internalWidth,
                height = cellSize * MapManager.internalHeight;

        areas = Stream.concat(
                IntStream.range(0, used.length).filter(i -> used[i])
                        .mapToObj(obstacles::get).map(Obstacle::getArea),
                avoided.getZones().stream()
                        .map(zi -> RectangleImpl.ofSize(zi.x * width, zi.y * height, width, height)))
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull Iterator<AreaImpl> iterator() {
        return areas.iterator();
    }
}
