package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.core.utils.pathfinder.Area;

import static com.github.manolo8.darkbot.Main.API;

public class Zone
        extends Entity {

    private final Area area = new Area(0, 0, 0, 0);

    Zone(int id) {
        super(id);
    }

    @Override
    public void update() {
        super.update();

        Location now = locationInfo.now;

        area.minX = now.x;
        area.minY = now.y;
        area.maxX = now.x + API.readMemoryDouble(address + 232);
        area.maxY = now.y + API.readMemoryDouble(address + 240);
    }

    @Override
    public void update(long address) {
        super.update(address);
    }

    public Area getZone() {
        return area;
    }

}
