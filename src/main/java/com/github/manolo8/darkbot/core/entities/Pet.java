package com.github.manolo8.darkbot.core.entities;

import static com.github.manolo8.darkbot.Main.API;

public class Pet extends Ship implements eu.darkbot.api.entities.Pet {

    private int level, playerId;

    public Pet() {}

    public Pet(int id, long address) {
        super(id);
        this.update(address);
    }

    @Override
    public void update() {
        if (address == 0) return;
        super.update();
        id = API.readMemoryInt(address + 56);
        level = API.readMemoryInt(address, 0x130, 0x28, 0x28);
        playerId = API.readMemoryInt(address, 0x130, 0x30, 0x28);
    }

    @Override
    public void update(long address) {
        super.update(address);

        clickable.setRadius(0);
    }

    public int getLevel() {
        return level;
    }

    @Override
    public int getOwnerId() {
        return playerId;
    }

    public int getPlayerId() {
        return playerId;
    }
}
