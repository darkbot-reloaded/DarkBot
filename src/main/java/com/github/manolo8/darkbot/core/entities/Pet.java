package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.Main;

import java.util.Optional;

import static com.github.manolo8.darkbot.Main.API;

public class Pet extends Ship implements eu.darkbot.api.game.entities.Pet {

    private int level, playerId;
    private Ship owner;

    public Pet(Main main) {
        // Removed by default, set to true externally when actually set
        this.attackTarget.added(main);
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
        id = API.readInt(address + 56);
        level = API.readInt(address, 0x130, 0x28, 0x28);

        int newPlayerId = API.readInt(address, 0x130, 0x30, 0x28);

        if (playerId != newPlayerId || owner == null) {
            playerId = newPlayerId;
            owner = main.mapManager.entities.ships.stream()
                    .filter(ship -> ship.getId() == playerId)
                    .findFirst().orElse(null);
        }
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
