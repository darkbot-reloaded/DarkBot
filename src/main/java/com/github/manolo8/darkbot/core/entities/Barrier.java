package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.utils.pathfinder.Rectangle;

public class Barrier
        extends Zone
        implements Obstacle {

    private static final int BARRIER_RADIUS = 60;
    private Rectangle area = new Rectangle(0, 0, 0, 0);

    public Barrier(int id, long address) {
        super(id, address);
    }

    @Override
    public void update() {
        super.update();

        Rectangle zone = getZone();
        area.minX = zone.minX - BARRIER_RADIUS;
        area.minY = zone.minY - BARRIER_RADIUS;
        area.maxX = zone.maxX + BARRIER_RADIUS;
        area.maxY = zone.maxY + BARRIER_RADIUS;
    }

    @Override
    public Rectangle getArea() {
        return area;
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }

    @Override
    public boolean use() {
        return area.minX > 0 || area.minY > 0 ||
                area.maxX < MapManager.internalWidth || area.maxY < MapManager.internalHeight;
    }
}
