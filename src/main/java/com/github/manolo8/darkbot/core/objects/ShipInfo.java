package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.Location;

import static com.github.manolo8.darkbot.Main.API;

public class ShipInfo extends Updatable {

    public int speed;
    public double angle;
    public long target;
    private long keepTargetTime;
    public LocationInfo destination = new LocationInfo();
    private final LocationInfo currentLoc;

    public ShipInfo(LocationInfo currentLoc) {
        this.currentLoc = currentLoc;
    }

    @Override
    public void update() {
        long newTarget = API.readMemoryLong(address + 112);
        if (newTarget != 0 || keepTargetTime > System.currentTimeMillis()) {
            target = newTarget;
            if (target != 0) keepTargetTime = System.currentTimeMillis() + 500;
        }
        angle = Math.toRadians(API.readMemoryInt(API.readMemoryLong(address + 48) + 32));
        speed = API.readMemoryInt(API.readMemoryLong(address + 72) + 40);

        destination.update(API.readMemoryLong(address + 96));
        destination.update();

        calcSpeed();
    }

    private double averageSpeed, pastTimeNeeded;
    private Location pastDestination;

    private void calcSpeed() {
        if (speed > 50) return;

        double timeNeeded = API.readDouble(address, 104, 152);
        if (destination.now.equals(pastDestination) && pastTimeNeeded == timeNeeded) return;

        this.pastDestination = destination.now.copy();
        this.pastTimeNeeded = timeNeeded;

        double elapsed = API.readDouble(address, 104, 136);

        double newSpeed = currentLoc.distance(destination) / (timeNeeded - elapsed);
        if (newSpeed < 100) return;
        if (newSpeed > 1000) newSpeed = 1000;

        averageSpeed = averageSpeed * 0.75 + newSpeed * 0.25;
    }

    public double getSpeed() {
        return speed > 50 ? speed : averageSpeed;
    }
}
