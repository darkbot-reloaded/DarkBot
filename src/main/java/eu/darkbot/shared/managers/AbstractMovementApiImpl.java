package eu.darkbot.shared.managers;

import eu.darkbot.api.entities.Portal;
import eu.darkbot.api.managers.MovementAPI;
import eu.darkbot.api.objects.Location;
import org.jetbrains.annotations.NotNull;

// TODO: 07.06.2021
public abstract class AbstractMovementApiImpl implements MovementAPI {

    @Override
    public boolean moveToPortal(@NotNull Portal portal) {
        return false;
    }

    @Override
    public void jumpPortal(Portal portal) {

    }

    @Override
    public boolean isMoving() {
        return false;
    }

    @Override
    public boolean isOutOfMap() {
        return false;
    }

    @Override
    public Location getDestination() {
        return null;
    }

    @Override
    public Location getCurrentLocation() {
        return null;
    }

    @Override
    public void moveRandom() {

    }

    @Override
    public void stop(boolean currentLocation) {

    }

    @Override
    public boolean canMove(double x, double y) {
        return false;
    }

    @Override
    public void moveTo(double x, double y) {

    }

    @Override
    public double getClosestDistance(double x, double y) {
        return 0;
    }

    @Override
    public double getDistanceBetween(double x, double y, double ox, double oy) {
        return 0;
    }
}
