package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.core.utils.pathfinder.Area;

import static com.github.manolo8.darkbot.Main.API;

public class Barrier
        extends Zone
        implements Obstacle {

    private Area area = new Area(0, 0, 0, 0);

    public Barrier(int id) {
        super(id);
    }

    @Override
    public void update() {
        super.update();

        Area zone = getZone();
        area.minX = zone.minX - 60;
        area.minY = zone.minY - 60;
        area.maxX = zone.maxX + 60;
        area.maxY = zone.maxY + 60;
    }

    @Override
    public Area getArea() {
        return area;
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }

    @Override
    public boolean use() {
        return true;
    }
}
