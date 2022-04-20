package com.github.manolo8.darkbot.core.utils.pathfinder;

import eu.darkbot.api.game.other.Area;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.utils.PathFinder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PolygonImpl implements Area.Polygon {

    private final List<? extends Locatable> vertices;
    private final RectangleImpl bounds = new RectangleImpl();

    private boolean invalidBounds = true;

    public PolygonImpl(Locatable... vertices) {
        this.vertices = Arrays.asList(vertices);
    }

    public PolygonImpl(List<? extends Locatable> vertices) {
        this.vertices = vertices;
    }

    @Override
    public RectangleImpl getBounds() {
        if (!invalidBounds) return bounds;
        invalidBounds = false;

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Locatable vertex : getVertices()) {
            minX = Double.min(minX, vertex.getX());
            minY = Double.min(minY, vertex.getY());
            maxX = Double.max(maxX, vertex.getX());
            maxY = Double.max(maxY, vertex.getY());
        }
        bounds.set(minX, minY, maxX, maxY);
        return bounds;
    }

    public void invalidateBounds() {
        this.invalidBounds = true;
    }

    @Override
    public Locatable toSide(Locatable point) {
        return getBounds().toSide(point);
    }

    @Override
    public Collection<Locatable> getPoints(@NotNull PathFinder pf) {
        return getBounds().getPoints(pf);
    }

    @Override
    public boolean containsPoint(double x, double y) {
        if (getVertices().size() <= 2 || !getBounds().containsPoint(x, y))
            return false;

        boolean res = false;

        for (int i = 0, j = getVertices().size() - 1; i < getVertices().size(); j = i++) {
            Locatable a = getVertices().get(i);
            Locatable b = getVertices().get(j);

            if ((a.getX() > y != b.getY() > y)
                && (x < (b.getX() - a.getX()) * (y - a.getY()) / (b.getY() - a.getY()) + a.getX())) {

                res = !res;
            }
        }
        return res;
    }

    @Override
    public boolean intersectsLine(double x, double y, double x2, double y2) {
        for (int i = 0; i < getVertices().size(); i++) {
            Locatable a = getVertices().get(i);
            Locatable b = getVertices().get((i + 1) % getVertices().size());

            if (Area.linesIntersect(a.getX(), a.getY(), b.getX(), b.getY(),
                    x, y, x2, y2)) return true;
        }

        return false;
    }

    @Override
    public List<? extends Locatable> getVertices() {
        return vertices;
    }
}
