package eu.darkbot.api.objects;

public interface Location extends Locatable, Point {
    /**
     * Returns distance to location ox & oy
     */
    double distanceTo(double ox, double oy);

    default double distanceTo(Locatable other) {
        return distanceTo(other.getX(), other.getY());
    }

    /**
     * @return angle to other location as radians.
     */
    double angleTo(double ox, double oy);

    default double angleTo(Locatable other) {
        return angleTo(other.getX(), other.getY());
    }

    /**
     * Copies current location into a new {@link Location} object and returns it.
     */
    Location copy();
    Location copy(double plusX, double plusY);

    default Location copy(Location other) {
        return copy(other.getX(), other.getY());
    }

    /**
     * Sets current location into specified location.
     */
    Location setTo(double x, double y);

    default Location setTo(Locatable other) {
        return setTo(other.getX(), other.getY());
    }

    /**
     * Adds given location to current.
     * Equals {@code currentX + plusX, currentY + plusY}
     */
    default Location plus(double plusX, double plusY) {
        return setTo(getX() + plusX, getY() + plusY);
    }

    default Location plus(Locatable other) {
        return plus(other.getX(), other.getY());
    }
}
