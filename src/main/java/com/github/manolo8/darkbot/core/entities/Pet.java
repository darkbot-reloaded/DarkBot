package com.github.manolo8.darkbot.core.entities;

import java.util.Optional;

import static com.github.manolo8.darkbot.Main.API;

public class Pet extends Ship implements eu.darkbot.api.game.entities.Pet {

    private int level, playerId;
    private Ship owner;

    public Pet() {
        // Removed by default, set to true externally when actually set
        this.removed = true;
    }

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

        if (owner == null)
            owner = main.mapManager.entities.ships.stream()
                    .filter(ship -> ship.getPet().map(p -> p == this).orElse(false))
                    .findFirst().orElse(null);
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

    @Override
    public Optional<eu.darkbot.api.game.entities.Ship> getOwner() {
        return Optional.ofNullable(owner);
    }

    public int getPlayerId() {
        return playerId;
    }
}
