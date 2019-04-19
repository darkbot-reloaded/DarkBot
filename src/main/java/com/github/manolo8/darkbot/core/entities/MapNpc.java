package com.github.manolo8.darkbot.core.entities;

public class MapNpc extends Npc {

    public MapNpc(int id) {
        super(id);
    }

    @Override
    public void update() {
        locationInfo.update();
    }

}
