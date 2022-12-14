package com.github.manolo8.darkbot.core.entities;

public class MapNpc extends Npc {

    public MapNpc(int id, long address) {
        super(id, address);
    }

    @Override
    public void update() {
        locationInfo.update();
        clickable.update();
    }

    @Override
    public boolean isMoving() {
        return locationInfo.isMoving();
    }
}
