package eu.darkbot.api.objects;

public interface LocationInfo extends Location {
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
     * Calculates future destination of entity in time(ms)
     */
    Location destinationInTime(long time);

    /**
     * @return current {@link Location}
     */
    Location getLocation();
}
