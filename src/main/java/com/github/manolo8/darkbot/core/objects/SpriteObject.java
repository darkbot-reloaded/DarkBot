package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import eu.darkbot.api.game.other.Point;

public class SpriteObject extends Updatable implements Point {
    //public int composite;

    private double x, y;

    @Override
    public void update() {
        // composite = Main.API.readInt(address + 8);

        long posHolder = Main.API.readLong(address + 72);
        x = Main.API.readInt(posHolder + 88) / 20d;
        y = Main.API.readInt(posHolder + 92) / 20d;
    }

    @Override
    public String toString() {
        return "SpriteObject{" +
                "x=" + x +
                ", y=" + y +
                '}';
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