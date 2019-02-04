package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.core.utils.pathfinder.Area;

import static com.github.manolo8.darkbot.Main.API;

public class Barrier
        extends Entity
        implements Obstacle {

    private final Area area;

    public Barrier(int id) {
        super(id);
        this.area = new Area(0, 0, 0, 0);
    }

    @Override
    public void update() {
        super.update();

        Location now = locationInfo.now;

        area.minX = now.x - 60;
        area.minY = now.y - 60;
        area.maxX = now.x + API.readMemoryDouble(address + 232) + 60;
        area.maxY = now.y + API.readMemoryDouble(address + 240) + 60;
    }

    @Override
    public void update(long address) {
        super.update(address);
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
