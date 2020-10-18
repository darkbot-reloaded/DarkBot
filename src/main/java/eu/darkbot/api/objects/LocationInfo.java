package eu.darkbot.api.objects;

import eu.darkbot.api.entities.Entity;
import eu.darkbot.utils.Location;

public interface LocationInfo {
    /**
     * @return true if current entity is loaded and valid.
     */
    boolean isLoaded();

    /**
     * @return true if current entity is moving.
     */
    boolean isMoving();

    /**
     * @return speed of entity.
     */
    double getSpeed();

    /**
     * @return current entity angle as radians.
     */
    double getAngle();

    /**
     * @return angle to other as radians.
     */
    double getAngle(double x, double y);

    default double getAngle(Entity other) {
        return getAngle(other.getLocationInfo());
    }

    default double getAngle(LocationInfo other) {
        return getAngle(other.getLocation());
    }

    default double getAngle(Location other) {
        return getAngle(other.x, other.y);
    }

    /**
     * @return distance to other
     */
    double distance(double x, double y);

    default double distance(Entity other) {
        return distance(other.getLocationInfo());
    }

    default double distance(LocationInfo other) {
        return distance(other.getLocation());
    }

    default double distance(Location other) {
        return distance(other.x, other.y);
    }

    /**
     * @return current {@link Location}
     */
    Location getLocation();

    /**
     * Calculates future destination of entity in time(ms)
     */
    Location destinationInTime(long time);
}
