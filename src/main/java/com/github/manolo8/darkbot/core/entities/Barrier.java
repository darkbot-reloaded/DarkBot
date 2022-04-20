package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.utils.pathfinder.RectangleImpl;
import eu.darkbot.api.game.other.Area;

public class Barrier
        extends Zone
        implements Obstacle, eu.darkbot.api.game.entities.Barrier {

    private static final int BARRIER_RADIUS = 60;
    private final RectangleImpl area = new RectangleImpl();

    public Barrier(int id, long address) {
        super(id, address);
    }

    @Override
    public void update() {
        super.update();

        Area.Rectangle zone = getZoneArea().getBounds();
        area.set(zone.getX() - BARRIER_RADIUS, zone.getY() - BARRIER_RADIUS,
                zone.getX2() + BARRIER_RADIUS, zone.getY2() + BARRIER_RADIUS);
    }

    @Override
    public RectangleImpl getArea() {
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
