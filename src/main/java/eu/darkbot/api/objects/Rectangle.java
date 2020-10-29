package eu.darkbot.api.objects;

import eu.darkbot.api.entities.utils.Area;

public interface Rectangle extends Area, Locatable {

    double getWidth();
    double getHeight();

    default double getX2() {
        return getX() + getWidth();
    }

    default double getY2() {
        return getY() + getHeight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default Rectangle getBounds() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean isEmpty() {
        return getWidth() <= 0 && getHeight() <= 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean containsPoint(double x, double y) {
        return (x >= getX() &&
                y >= getY() &&
                x < getX2() &&
                y < getY2());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean intersects(double x, double y, double w, double h) {
        if (isEmpty() || w <= 0 || h <= 0) return false;

        return (x + w > getX() &&
                y + h > getY() &&
                x < getX2() &&
                y < getY2());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean intersectsLine(double x, double y, double x2, double y2) {
        return  (containsPoint(x, y) || containsPoint(x2, y2)) ||
                (x < x2 && Area.linesIntersect(x, y, x2, y2, getX(), getY(), getX(), getY2())) ||
                (x > x2 && Area.linesIntersect(x, y, x2, y2, getX2(), getY(), getX2(), getY2())) ||
                (y < y2 && Area.linesIntersect(x, y, x2, y2, getX(), getY(), getX2(), getY())) ||
                (y > y2 && Area.linesIntersect(x, y, x2, y2, getX(), getY2(), getX2(), getY2()));
    }
}
