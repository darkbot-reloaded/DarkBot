package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.manager.HeroManager;
import eu.darkbot.api.game.entities.Pet;
import eu.darkbot.api.game.items.SelectableItem;

import java.util.Optional;

import static com.github.manolo8.darkbot.Main.API;

public class Player extends Ship implements eu.darkbot.api.game.entities.Player {

    protected com.github.manolo8.darkbot.core.entities.Pet pet;

    public Player() {}

    public Player(int id, long address) {
        super(id, address);
    }

    @Override
    public void update() {
        super.update();

        if (this instanceof HeroManager) return;

        long petAddress = API.readMemoryLong(address + 176);

        if (petAddress == 0) pet = null;
        else if (pet == null || petAddress != pet.address)
            pet = main.mapManager.entities.pets.stream()
                    .filter(p -> p.address == petAddress)
                    .findAny()
                    .orElse(null);
    }

    @Override
    public boolean hasPet() {
        return pet != null;
    }

    @Override
    public Optional<Pet> getPet() {
        return Optional.ofNullable(pet);
    }

    @Override
    public SelectableItem.Formation getFormation() {
        return SelectableItem.Formation.of(formationId);
    }

    @Override
    public boolean isInFormation(int formationId) {
        return formationId == this.formationId;
    }
}
