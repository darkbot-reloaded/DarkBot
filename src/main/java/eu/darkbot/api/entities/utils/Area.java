package eu.darkbot.api.entities.utils;

import eu.darkbot.api.objects.Locatable;
import eu.darkbot.api.objects.Rectangle;

public interface Area extends Rectangle {

    Locatable[] getPoints();

    default boolean isEmpty() {
        return getWidth() <= 0 && getHeight() <= 0;
    }

    default boolean containsPoint(double x, double y) {
        return (x >= getX() &&
                y >= getY() &&
                x < getX2() &&
                y < getY2());
    }

    default boolean containsPoint(Locatable point) {
        return containsPoint(point.getX(), point.getY());
    }

    default boolean intersects(double x, double y, double w, double h) {
        if (isEmpty() || w <= 0 || h <= 0) return false;

        return (x >= getX() &&
                y >= getY() &&
                (x + w) <= getX2() &&
                (y + h) <= getY2());
    }

    default boolean intersects(Rectangle rect) {
        return intersects(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    default boolean intersectsLine(double x, double y, double x2, double y2) {
        return  (containsPoint(x, y) || containsPoint(x2, y2)) ||
                (x < x2 && linesIntersect(x, y, x2, y2, getX(), getY(), getX(), getY2())) ||
                (x > x2 && linesIntersect(x, y, x2, y2, getX2(), getY(), getX2(), getY2())) ||
                (y < y2 && linesIntersect(x, y, x2, y2, getX(), getY(), getX2(), getY())) ||
                (y > y2 && linesIntersect(x, y, x2, y2, getX(), getY2(), getX2(), getY2()));
    }

    default boolean intersectsLine(Locatable startPoint, Locatable endPoint) {
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