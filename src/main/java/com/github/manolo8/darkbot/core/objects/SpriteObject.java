package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import eu.darkbot.api.game.other.Point;

public class SpriteObject extends Updatable.Auto implements Point {
    public int composite;

    public double x, y;

    @Override
    public void update() {
        // composite = Main.API.readInt(address + 8);
        x = (Main.API.readInt(address, 0x48, 0x58)) / 20d;
        y = (Main.API.readInt(address, 0x48, 0x5C)) / 20d;
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