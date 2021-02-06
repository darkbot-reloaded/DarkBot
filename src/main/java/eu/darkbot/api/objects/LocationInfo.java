package eu.darkbot.api.objects;

/**
 * Represents in-game {@link eu.darkbot.api.entities.Entity}'s location point.
 */
public interface LocationInfo extends Location {

    /**
     * @return true if current entity is moving.
     */
    boolean isMoving();

    /**
     * @return speed of entity based on traveled distance.
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
     * @return current {@link Location} of this {@link LocationInfo}
     */
    Location getCurrent();

    /**
     * @return previous {@link Location} of this {@link LocationInfo}
     */
    Location getLast();

    /**
     * @return even earlier {@link Location} of this {@link LocationInfo}
     */
    Location getPast();

    @Override
    default double getX() {
        return getCurrent().getX();
    }

    @Override
    default double getY() {
        return getCurrent().getY();
    }

    @Override
    default Location copy() {
        return getCurrent().copy();
    }

    @Override
    default Location setTo(double x, double y) {
        return getCurrent().setTo(x, y);
    }
}
