package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;

import static com.github.manolo8.darkbot.Main.API;

public class Point extends Updatable {

    public double x;
    public double y;

    public Point(long address) {
        this.address = address;
    }

    public void update() {
        this.x = API.readMemoryDouble(address + 32);
        this.y = API.readMemoryDouble(address + 40);
    }
}
