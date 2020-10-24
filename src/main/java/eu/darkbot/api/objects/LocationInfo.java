package eu.darkbot.api.objects;

import eu.darkbot.utils.Location;

public interface LocationInfo extends Locatable {
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

    default double getAngle(Locatable other) {
        return getAngle(other.getX(), other.getY());
    }

    /**
     * @return distance to other
     */
    double distance(double x, double y);

    default double distance(Locatable other) {
        return distance(other.getX(), other.getY());
    }

    /**
     * Calculates future destination of entity in time(ms)
     */
    Location destinationInTime(long time);

    Location getLocation();
}
