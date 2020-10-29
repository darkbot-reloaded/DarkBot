package eu.darkbot.api.objects;

import eu.darkbot.api.entities.utils.Area;

public interface Circle extends Area, Locatable{

    double getRadius();

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean isEmpty() {
        return getRadius() <= 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean containsPoint(double x, double y) {
        x -= getX();
        y -= getY();
        return Math.sqrt(x * x + y * y) <= getRadius();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean intersectsLine(double x, double y, double x2, double y2) {
        if (containsPoint(x, y) || containsPoint(x2, y2)) return true;

        x2 -= x;
        y2 -= y;
        x -= getX();
        y -= getY();

        double dot = x * x2 + y * y2;
        double len = x * x + y * y - (dot * dot / (x2 * x2 + y2 * y2));

        if (len < 0) len = 0;
        return Math.sqrt(len) <= getRadius();
    }
}
