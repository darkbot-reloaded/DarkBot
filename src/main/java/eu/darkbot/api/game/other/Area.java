package eu.darkbot.api.game.other;

import eu.darkbot.api.utils.PathFinder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A geometric area that covers some 2D space on the map.
 */
public interface Area {

    /**
     * @return true if line {@code x1, y1 & x2, y2} intersects line {@code x3, y3 & x4, y4}
     */
    static boolean linesIntersect(double x1, double y1,
                                  double x2, double y2,
                                  double x3, double y3,
                                  double x4, double y4) {
        final double v = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        final double uA = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / v;
        final double uB = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / v;

        return uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1;
    }

    /**
     * Returns an {@link Rectangle} that completely encloses the
     * {@link Area}.  Note that there is no guarantee that the
     * returned {@link Rectangle} is the smallest bounding box that
     * encloses the {@link Area}, only that the {@link Area}
     * lies entirely within the indicated {@link Rectangle}.
     */
    Rectangle getBounds();

    /**
     * @param point The original point to start at
     * @return The location outside the area that is closest to the original
     */
    Locatable toSide(Locatable point);

    /**
     * Generates navigation points from the sharp corners around the area
     * @param pf The current path finder to filter points on
     * @return The list of points to use for pathfinding around this area
     */
    Collection<? extends Locatable> getPoints(@NotNull PathFinder pf);

    /**
     * @return true if Area is empty.
     */
    default boolean isEmpty() {
        return getBounds().isEmpty();
    }

    /**
     * @return true if Area contains point of x & y.
     */
    boolean containsPoint(double x, double y);

    default boolean containsPoint(@NotNull Locatable point) {
        return containsPoint(point.getX(), point.getY());
    }

    /**
     * Tests if given line coordinates intersects this {@link Area}.
     */
    boolean intersectsLine(double x, double y, double x2, double y2);

    default boolean intersectsLine(@NotNull Locatable startPoint, @NotNull Locatable endPoint) {
        return intersectsLine(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
    }

    /**
     * {@link Circle} area object
     */
    interface Circle extends Area, Locatable {

        /**
         * @return radius of the {@link Circle}
         */
        double getRadius();
    }

    /**
     * {@link Rectangle} area object
     */
    interface Rectangle extends Area, Locatable {

        /**
         * @return width of the {@link Rectangle}
         */
        default double getWidth() {
            return getX2() - getX();
        }

        /**
         * @return height of the {@link Rectangle}
         */
        default double getHeight() {
            return getY2() - getY();
        }

        /**
         * @return second x coordinate of the {@link Rectangle}
         */
        double getX2();

        /**
         * @return second y coordinate of the {@link Rectangle}
         */
        double getY2();

        /**
         * {@inheritDoc}
         */
        @Override
        default Rectangle getBounds() {
            return this;
        }

        default boolean containsPoint(double x, double y) {
            return (getX() <= x && x <= getX2() &&
                    getY() <= y && y <= getY2());
        }

    }
}