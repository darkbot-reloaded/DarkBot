package eu.darkbot.api.entities.utils;

import eu.darkbot.api.objects.Locatable;
import eu.darkbot.api.objects.Rectangle;
import eu.darkbot.api.utils.PathFinder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface Area {

    /**
     Returns an {@link Rectangle} that completely encloses the
     * {@link Area}.  Note that there is no guarantee that the
     * returned {@link Rectangle} is the smallest bounding box that
     * encloses the {@link Area}, only that the {@link Area}
     * lies entirely within the indicated {@link Rectangle}.
     */
    Rectangle getBounds();

    Collection<Locatable> getPoints(@NotNull PathFinder pf);

    /**
     * @return true if Area is empty.
     */
    boolean isEmpty();

    /**
     * @return true if Area contains point of x&y.
     */
    boolean containsPoint(double x, double y);

    default boolean containsPoint(@NotNull Locatable point) {
        return containsPoint(point.getX(), point.getY());
    }

    /**
     * Tests if given coordinates are in this {@link Area}.
     */
    boolean intersects(double x, double y, double w, double h) ;

    default boolean intersects(@NotNull Rectangle rect) {
        return intersects(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    /**
     * Tests if given line coordinates intersects this {@link Area}.
     */
    boolean intersectsLine(double x, double y, double x2, double y2);

    default boolean intersectsLine(@NotNull Locatable startPoint, @NotNull Locatable endPoint) {
        return intersectsLine(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
    }

    static boolean linesIntersect(double x1, double y1,
                                  double x2, double y2,
                                  double x3, double y3,
                                  double x4, double y4) {
        final double v = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        final double uA = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / v;
        final double uB = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / v;

        return uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1;
    }
}