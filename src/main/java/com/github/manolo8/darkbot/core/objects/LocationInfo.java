package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.Location;

import static com.github.manolo8.darkbot.Main.API;

public class LocationInfo extends Updatable implements eu.darkbot.api.objects.LocationInfo {

    public Location now = new Location();
    public Location last = new Location();
    public Location past = new Location();

    private long lastUpdate;
    public double angle;
    public double speed;

    public LocationInfo() {}

    public LocationInfo(long address) {
        this.address = address;
    }

    public LocationInfo(double x, double y) {
        this.now.x = x;
        this.now.y = y;
    }

    @Override
    public void update() {
        if (address == 0) return;
        double newX = API.readMemoryDouble(address + 32);
        double newY = API.readMemoryDouble(address + 40);
        updatePosition(newX, newY);
    }

    public void updatePosition(double newX, double newY) {
        // Update only if both x and y changed, or >100 ms since last update
        if ((newX == now.x || newY == now.y) && System.currentTimeMillis() - lastUpdate < 100) return;
        past.x = last.x;
        past.y = last.y;

        last.x = now.x;
        last.y = now.y;

        now.x = newX;
        now.y = newY;

        if (isMoving() && !(last.x == 0 && last.y == 0)) {
            angle = now.angle(past);
            double newSpeed = now.distance(last) / (System.currentTimeMillis() - lastUpdate);
            speed = Math.min(1, speed * 0.75 + newSpeed * 0.25); // In-game speed of 560 is a 0.56 speed here, limit to 1000.
        } else {
            speed = 0;
        }

        lastUpdate = System.currentTimeMillis();
    }

    public Location destinationInTime(long time) {

        Location destination = new Location();

        if (last.x != 0 && last.y != 0) {
            double move = speed * time;

            destination.x = now.x + Math.cos(angle) * move;
            destination.y = now.y + Math.sin(angle) * move;
        } else {
            destination.x = now.x;
            destination.y = now.y;
        }

        return destination;
    }

    public double distance(double x, double y) {
        return now.distance(x, y);
    }

    public double distance(LocationInfo locationInfo) {
        return now.distance(locationInfo.now);
    }

    public double distance(Location location) {
        return now.distance(location);
    }

    public double distance(Entity entity) {
        return now.distance(entity.locationInfo.now);
    }

    public boolean isLoaded() {
        return address != 0;
    }

    public boolean isMoving() {
        return !now.equals(last) || !now.equals(past);
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public double getAngle() {
        return angle;
    }

    @Override
    public eu.darkbot.api.objects.Location getCurrent() {
        return now;
    }

    @Override
    public eu.darkbot.api.objects.Location getLast() {
        return last;
    }

    @Override
    public eu.darkbot.api.objects.Location getPast() {
        return past;
    }
}