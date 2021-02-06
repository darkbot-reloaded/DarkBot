package eu.darkbot.api.objects;

import org.jetbrains.annotations.NotNull;

/**
 * Represents location point in-game.
 */
public interface Locatable {
    /**
     * Creates new instance of {@link Locatable} with given parameters.
     * @param x coordinate
     * @param y coordinate
     * @return {@link Locatable} with given coordinates
     */
    static Locatable of(double x, double y) {
        return new LocatableImpl(x, y);
    }

    /**
     * @return y coordinate of the {@link Locatable}
     */
    double getX();

    /**
     * @return y coordinate of the {@link Locatable}
     */
    double getY();


    /**
     * @return the distance between current {@link Location} and other.
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

    //Locatable implementation
    class LocatableImpl implements Locatable {
        private final double x;
        private final double y;

        public LocatableImpl(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() { return x; }

        public double getY() { return y; }
    }
}
