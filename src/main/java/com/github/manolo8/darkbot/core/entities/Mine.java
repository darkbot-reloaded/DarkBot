package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.utils.pathfinder.Area;
import com.github.manolo8.darkbot.core.utils.pathfinder.Circle;

import static com.github.manolo8.darkbot.Main.API;

public class Mine extends Entity implements Obstacle {
    // A bit of a bigger avoid area on these
    private static final int FROZEN_LAB_MINE = 21;

    public int typeId;

    private final Circle area = new Circle(0, 0, 200);

    public Mine(int id, long address) {
        super(id);
        this.update(address);
    }

    @Override
    public void update(long address) {
        super.update(address);

        this.typeId = API.readMemoryInt(address + 112);
    }

    @Override
    public void update() {
        super.update();

        if (locationInfo.isMoving())
            area.set(locationInfo.now, typeId == FROZEN_LAB_MINE ? 500 : 200);
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

    @Override
    public String toString() {
        return typeId + "";
    }
}
