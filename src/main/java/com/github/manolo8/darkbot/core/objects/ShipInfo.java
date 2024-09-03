package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.Location;

import java.util.Optional;

import static com.github.manolo8.darkbot.Main.API;

public class ShipInfo extends Updatable {

    public int speed;
    public double angle, destinationAngle = Double.MIN_VALUE;
    public long target;

    public LocationInfo destination = new LocationInfo();

    private long keepTargetTime;
    private double predictedSpeed, pastTimeNeeded;

    private Location pastDestination;
    private final LocationInfo entityLocation;

    private long lastMovement;
    private boolean moving;

    public ShipInfo(LocationInfo entityLocation) {
        this.entityLocation = entityLocation;
    }

    public double getSpeed() {
        return speed == 1 ? predictedSpeed : speed;
    }

    public Optional<eu.darkbot.api.game.other.Location> getDestination() {
        return destination.address == 0 ? Optional.empty() : Optional.of(destination);
    }

    @Override
    public void update() {
        long newTarget = API.readLong(address + 120);
        if (newTarget != 0 || keepTargetTime > System.currentTimeMillis()) {
            target = newTarget;
            if (target != 0) keepTargetTime = System.currentTimeMillis() + 1000;
        }
        angle = Math.toRadians(API.readInt(API.readLong(address + 56) + 32));
        speed = API.readInt(API.readLong(address + 80) + 40);

        destination.update(API.readLong(address + 104));
        destination.update();

        updateSpeedAndAngle();
        moving = destination.isInitialized() && entityLocation.distanceTo(destination) > 0;
        if (moving) lastMovement = System.currentTimeMillis();
    }

    private void updateSpeedAndAngle() {
        if (!destination.isInitialized() || destination.now.equals(pastDestination)) return;
        this.pastDestination = destination.now.copy();

        this.destinationAngle = entityLocation.angleTo(destination);

        if (speed != 1) return; // Entities with a valid real speed do not need to have this speed prediction done

        long tweenLiteAddress = API.readLong(address, 112);

        double timeNeeded = API.readDouble(tweenLiteAddress, 152);
        if (pastTimeNeeded == timeNeeded) return;

        this.pastTimeNeeded = timeNeeded;

        double elapsed = API.readDouble(tweenLiteAddress, 136); // Offset `144` works too
        double newSpeed = Math.min(1000, entityLocation.distance(destination) / (timeNeeded - elapsed));

        if (newSpeed < 100) return; // Probably an invalid speed, ignore it

        if (predictedSpeed == 0) predictedSpeed = newSpeed;
        else predictedSpeed = predictedSpeed * 0.75 + newSpeed * 0.25;
    }

    public boolean isMoving() {
        return moving;
    }

    public boolean isMoving(long time) {
        return lastMovement + time >= System.currentTimeMillis();
    }
}
