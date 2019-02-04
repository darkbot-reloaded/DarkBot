package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.objects.PlayerInfo;
import com.github.manolo8.darkbot.core.utils.pathfinder.Area;

import static com.github.manolo8.darkbot.Main.API;

public class BattleStation
        extends Entity
        implements Obstacle {

    public PlayerInfo info;
    public Area area;

    public BattleStation(int id) {
        super(id);

        this.info = new PlayerInfo();
        this.area = new Area(0, 0, 0, 0);
    }

    @Override
    public void update() {
        super.update();

        info.update();
        if (locationInfo.isMoving())
            area.set(locationInfo.now, 300, 300);
    }

    @Override
    public void update(long address) {
        super.update(address);

        info.update(API.readMemoryLong(address + 120));
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
        return info.isEnemy();
    }
}
