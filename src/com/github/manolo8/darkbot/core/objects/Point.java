package com.github.manolo8.darkbot.core.objects;

import static com.github.manolo8.darkbot.Main.API;

public class Point {

    private long address;
    public double x;
    public double y;

    public Point(long address) {
        this.address = address;
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        this.x = API.readMemoryDouble(address + 32);
        this.y = API.readMemoryDouble(address + 40);
    }

    public void update(long address) {
        this.address = address;
    }
}
