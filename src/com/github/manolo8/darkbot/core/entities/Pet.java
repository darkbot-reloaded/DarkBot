package com.github.manolo8.darkbot.core.entities;

import static com.github.manolo8.darkbot.Main.API;

public class Pet extends Ship {

    public boolean alive;

    public Pet(long address, int id) {
        super(address, id);
    }

    @Override
    public void update() {
        super.update();
        id = API.readMemoryInt(address + 56);
    }
}
