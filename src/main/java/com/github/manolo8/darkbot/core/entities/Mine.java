package com.github.manolo8.darkbot.core.entities;

import static com.github.manolo8.darkbot.Main.API;

public class Mine extends Entity {
    public int typeId;

    public Mine(int id, long address) {
        super(id);
        this.update(address);
    }

    @Override
    public void update(long address) {
        super.update(address);

        this.typeId = API.readMemoryInt(address + 112);
    }
}
