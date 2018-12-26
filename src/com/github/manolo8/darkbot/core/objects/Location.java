package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.entities.Entity;

import static com.github.manolo8.darkbot.Main.API;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.StrictMath.atan2;

public class Location implements Updatable {

    private long address;
    public double x;
    public double y;
    public double lastX;
    public double lastY;

    public Location(long address) {
        this.address = address;
    }

    public Location(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void update() {

        lastX = x;
        lastY = y;
        x = API.readMemoryDouble(address + 32);
        y = API.readMemoryDouble(address + 40);

    }

    @Override
    public void update(long address) {
        this.address = address;
    }

    public boolean isLoaded() {
        return x != 0 && y != 0;
    }

    public double distance(Entity entity) {
        return distance(entity.location);
    }

    public double distance(double ox, double oy) {
        return sqrt(pow(x - ox, 2) + pow(y - oy, 2));
    }

    public double measureSpeed() {
        return distance(lastX, lastY) * 10;
    }

    public double angle(Location other) {
        return atan2(y - other.y, x - other.x);
    }

    public boolean isMoving() {
        return lastX != x || lastY != y;
    }

    public double distance(Location loc) {
        return distance(loc.x, loc.y);
    }

    public Location add(int x, int y) {
        return new Location(this.x + x, this.y + y);
    }
}