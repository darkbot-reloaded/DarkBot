package eu.darkbot.api.entities.utils;

import eu.darkbot.api.objects.Locatable;
import eu.darkbot.api.objects.Rectangle;

import java.awt.geom.Line2D;

public interface Area extends Rectangle {

    Locatable[] getPoints();

    default boolean isEmpty() {
        return getWidth() <= 0 && getHeight() <= 0;
    }

    default boolean containsPoint(double x, double y) {
        double x0 = getX();
        double y0 = getY();
        return (x >= x0 &&
                y >= y0 &&
                x < x0 + getWidth() &&
                y < y0 + getHeight());
    }

    default boolean containsPoint(Locatable point) {
        return containsPoint(point.getX(), point.getY());
    }

    default boolean intersects(double x, double y, double w, double h) {
        if (isEmpty() || w <= 0 || h <= 0) return false;

        double x0 = getX();
        double y0 = getY();
        return (x >= x0 &&
                y >= y0 &&
                (x + w) <= x0 + getWidth() &&
                (y + h) <= y0 + getHeight());
    }

    default boolean intersects(Rectangle rect) {
        return intersects(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    default boolean intersectsLine(double x, double y, double x2, double y2) {
        return  (containsPoint(x, y) || containsPoint(x2, y2)) ||
                (x < x2 && Line2D.linesIntersect(x, y, x2, y2, getX(), getY(), getX(), getY2())) ||
                (x > x2 && Line2D.linesIntersect(x, y, x2, y2, getX2(), getY(), getX2(), getY2())) ||
                (y < y2 && Line2D.linesIntersect(x, y, x2, y2, getX(), getY(), getX2(), getY())) ||
                (y > y2 && Line2D.linesIntersect(x, y, x2, y2, getX(), getY2(), getX2(), getY2()));
    }

    default boolean intersectsLine(Locatable startPoint, Locatable endPoint) {
        return intersectsLine(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
    }
}