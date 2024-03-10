package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;

import static com.github.manolo8.darkbot.Main.API;

public class Point extends Updatable.Auto implements eu.darkbot.api.game.other.Point {

    public double x;
    public double y;

    public void update() {
        if (address == 0) return;
        this.x = API.readDouble(address + 32);
        this.y = API.readDouble(address + 40);
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }
}
