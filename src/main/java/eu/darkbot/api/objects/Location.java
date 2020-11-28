package eu.darkbot.api.objects;

import org.jetbrains.annotations.NotNull;

public interface Location extends Locatable, Point {

    /**
     * @return the distance from the current point {@code (getX(), getY())}
     *         to the point {@code (ox, oy)}
     */
    default double distanceTo(double ox, double oy) {
        ox -= getX();
        oy -= getY();
        return Math.sqrt(ox * ox + oy * oy);
    }

    default double distanceTo(@NotNull Locatable other) {
        return distanceTo(other.getX(), other.getY());
    }

    /**
     * @return angle to other location as radians.
     */
    default double angleTo(double ox, double oy) {
        return Math.atan2(getY() - oy, getX() - ox);
    }

    default double angleTo(@NotNull Locatable other) {
        return angleTo(other.getX(), other.getY());
    }

    /**
     * Copies current location into a new {@link Location} object and returns it.
     */
    Location copy();

    /**
     * Sets current location into specified location.
     */
    Location setTo(double x, double y);

    default Location setTo(@NotNull Locatable other) {
        return setTo(other.getX(), other.getY());
    }

    /**
     * Adds given location to current.
     * Equals {@code currentX + plusX, currentY + plusY}
     */
    default Location plus(double plusX, double plusY) {
        return setTo(getX() + plusX, getY() + plusY);
    }

    default Location plus(@NotNull Locatable other) {
        return plus(other.getX(), other.getY());
    }
}
