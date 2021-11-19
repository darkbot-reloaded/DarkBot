package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.Location;

import static com.github.manolo8.darkbot.Main.API;

public class ShipInfo extends Updatable {

    public int speed;
    public double angle;
    public long target;

    public LocationInfo destination = new LocationInfo();

    private long keepTargetTime;
    private double predictedSpeed, pastTimeNeeded;

    private Location pastDestination;
    private final LocationInfo entityLocation;

    public ShipInfo(LocationInfo entityLocation) {
        this.entityLocation = entityLocation;
    }

    public double getSpeed() {
        return speed == 1 ? predictedSpeed : speed;
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

        updateSpeed();
    }

    private void updateSpeed() {
        if (speed != 1) return; // Entities with a valid real speed do not need to have this speed prediction done

        long tweenLiteAddress = API.readLong(address, 104);

        double timeNeeded = API.readDouble(tweenLiteAddress, 152);
        if (destination.now.equals(pastDestination) && pastTimeNeeded == timeNeeded) return;

        this.pastDestination = destination.now.copy();
        this.pastTimeNeeded = timeNeeded;

        double elapsed = API.readDouble(tweenLiteAddress, 136); // Offset `144` works too
        double newSpeed = Math.min(1000, entityLocation.distance(destination) / (timeNeeded - elapsed));

        if (newSpeed < 100) return; // Probably an invalid speed, ignore it

        if (predictedSpeed == 0) predictedSpeed = newSpeed;
        else predictedSpeed = predictedSpeed * 0.75 + newSpeed * 0.25;
    }
}
