package eu.darkbot.utils;

import eu.darkbot.api.objects.Locatable;

// in future Location extends Point?
public class Point implements Locatable {
    public double x, y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }
}
