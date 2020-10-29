package eu.darkbot.api.objects;

public interface Location extends Locatable, Point {
    /**
     * Returns distance to location ox & oy
     */
    default double distanceTo(double ox, double oy) {
        ox -= getX();
        oy -= getY();
        return Math.sqrt(ox * ox + oy * oy);
    }

    default double distanceTo(Locatable other) {
        return distanceTo(other.getX(), other.getY());
    }

    default double angleTo(double ox, double oy) {
        return Math.atan2(getY() - oy, getX() - ox);
    }

    default double angleTo(Locatable other) {
        return angleTo(other.getX(), other.getY());
    }

    Location setTo(double x, double y);

    default Location setTo(Locatable other) {
        return setTo(other.getX(), other.getY());
    }

    default Location plus(double plusX, double plusY) {
        return setTo(getX() + plusX, getY() + plusY);
    }

    default Location plus(Locatable other) {
        return plus(other.getX(), other.getY());
    }

    Location copy();
    Location copy(double plusX, double plusY);

    default Location copy(Location other) {
        return copy(other.getX(), other.getY());
    }
}
