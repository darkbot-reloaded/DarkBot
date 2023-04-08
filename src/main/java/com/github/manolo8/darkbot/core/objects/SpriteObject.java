package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import eu.darkbot.api.game.other.Point;

public class SpriteObject extends Updatable implements Point {
    //public int composite;

    protected int x, y;

    @Override
    public void update() {
        // composite = Main.API.readInt(address + 8);

        // Flash internally calculates anything that uses pixels with twips (or 1/20 of a pixel).
        // Sprites, movie clips and any other object on the stage are positioned with twips.
        // As a result, the coordinates of (for example) sprites are always multiples of 0.05.
        long posHolder = Main.API.readLong(address + 72);
        x = Math.round(Main.API.readInt(posHolder + 88) * 0.05f);
        y = Math.round(Main.API.readInt(posHolder + 92) * 0.05f);
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

    @Override
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }
}