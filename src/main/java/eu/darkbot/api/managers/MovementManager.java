package eu.darkbot.api.managers;

import eu.darkbot.api.entities.Entity;
import eu.darkbot.api.entities.Portal;
import eu.darkbot.api.objects.LocationInfo;
import eu.darkbot.utils.Location;

public interface MovementManager {
    /**
     * Tries to jump through portal.
     *
     * @param portal to jump through
     * @return true if jump button was clicked
     */
    boolean jumpPortal(Portal portal);

    /**
     * @return true if hero is moving or destination path isn't empty.
     */
    boolean isMoving();

    /**
     * @return true if hero is out of map
     */
    boolean isOutOfMap();

    /**
     * @return current destination {@link Location}
     */
    Location getDestination();

    /**
     * @return current hero location.
     */
    Location getCurrentLocation();

    /**
     * Will move random, prefers `preferred zones`
     * and omits `avoided zones`
     */
    void moveRandom();

    /**
     * Stops ship, removes destination path.
     * @param currentLocation should stop at current location.
     */
    void stop(boolean currentLocation);

    /**
     * Checks if it is possible to move to your destination.
     * @return true if it is possible to move there.
     */
    boolean canMove(double x, double y);

    default boolean canMove(Location destination) {
        return canMove(destination.x, destination.y);
    }

    default boolean canMove(LocationInfo destination) {
        return canMove(destination.getLocation());
    }

    default boolean canMove(Entity destination) {
        return canMove(destination.getLocationInfo());
    }

    /**
     * Sets location where to move.
     */
    void moveTo(double x, double y);

    default void moveTo(Location destination) {
        moveTo(destination.x, destination.y);
    }

    default void moveTo(LocationInfo destination) {
        moveTo(destination.getLocation());
    }

    default void moveto(Entity destination) {
        moveTo(destination.getLocationInfo());
    }

    /**
     * Returns closest distance to destination,
     * calculates barriers and any other obstacles.
     */
    double getClosestDistance(double x, double y);

    default double getClosestDistance(Location destination) {
        return getClosestDistance(destination.x, destination.y);
    }

    default double getClosestDistance(LocationInfo destination) {
        return getClosestDistance(destination.getLocation());
    }

    default double getClosestDistance(Entity destination) {
        return getClosestDistance(destination.getLocationInfo());
    }

    /**
     * Calculates distance between two points,
     * includes barriers and any other obstacles.
     */
    double getDistanceBetween(double x, double y, double ox, double oy);

    default double getDistanceBetween(Location location, Location otherLocation) {
        return getDistanceBetween(location.x, location.y, otherLocation.x, otherLocation.y);
    }

    default double getDistanceBetween(LocationInfo location, LocationInfo otherLocation) {
        return getDistanceBetween(location.getLocation(), otherLocation.getLocation());
    }

    default double getDistanceBetween(Entity location, Entity otherLocation) {
        return getDistanceBetween(location.getLocationInfo(), otherLocation.getLocationInfo());
    }
}
