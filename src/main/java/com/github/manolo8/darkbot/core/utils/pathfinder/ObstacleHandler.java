package com.github.manolo8.darkbot.core.utils.pathfinder;

import com.github.manolo8.darkbot.config.ZoneInfo;
import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.manager.MapManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObstacleHandler implements Iterable<Area> {

    private MapManager map;
    private final List<Obstacle> obstacles;
    private ZoneInfo avoided;
    private int obstacleCount;
    private List<Area> areas = new ArrayList<>();

    public ObstacleHandler(MapManager map) {
        this.map = map;
        this.obstacles = map.entities.obstacles;
        this.avoided = new ZoneInfo(1);
    }

    boolean changed() {
        boolean changed = obstacles.size() != obstacleCount || map.avoided != avoided || avoided.changed ||
                obstacles.stream().anyMatch(o -> o.use() != o.getArea().cachedUsing || o.getArea().changed);
        if (changed) rebuildAreas();
        return changed;
    }

    public List<Area> get() {
        return areas;
    }

    public Stream<Area> stream() {
        return areas.stream();
    }

    private void rebuildAreas() {
        if (obstacles == null || map.avoided == null) return;
        obstacleCount = obstacles.size();
        avoided = map.avoided;

        double cellSize = 1d / avoided.resolution,
                width = cellSize * MapManager.internalWidth,
                height = cellSize * MapManager.internalHeight;

        areas = Stream.concat(obstacles.stream().peek(o -> o.getArea().changed = false)
                        .filter(o -> o.getArea().cachedUsing = o.use()).map(Obstacle::getArea),
                avoided.getZones().stream().map(zi -> Area.ofSize(zi.x * width, zi.y * height, width, height)))
                .collect(Collectors.toList());
    }

    @Override
    public Iterator<Area> iterator() {
        return areas.iterator();
    }
}
