package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.def.Updatable;

import static com.github.manolo8.darkbot.Main.API;

public class ShipInfo implements Updatable {

    private long address;

    public int speed;
    public long target;

    public ShipInfo() {
    }

    public ShipInfo(long address) {
        this.address = address;
    }

    @Override
    public void update() {

        target = API.readMemoryLong(address + 112);
        speed = API.readMemoryInt(API.readMemoryLong(address + 72) + 40);

    }

    @Override
    public void update(long address) {
        this.address = address;
    }
}
