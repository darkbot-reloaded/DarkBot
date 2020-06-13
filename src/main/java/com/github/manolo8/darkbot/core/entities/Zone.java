package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.pathfinder.Area;

import static com.github.manolo8.darkbot.Main.API;

public class Zone
        extends Entity {

    private final Area area = new Area(0, 0, 0, 0);
    private final ObjArray points = ObjArray.ofVector(true);

    Zone(int id, long address) {
        super(id);
        this.update(address);
    }

    @Override
    public void update() {
        super.update();

        points.update(API.readMemoryLong(address + 216));
        if (points.getSize() < 3) return;

        area.set(API.readMemoryDouble(points.get(0) + 32),
                 API.readMemoryDouble(points.get(1) + 40),
                 API.readMemoryDouble(points.get(2) + 32),
                 API.readMemoryDouble(points.get(2) + 40));
    }

    public Area getZone() {
        return area;
    }
}
