package eu.darkbot.utils;

import eu.darkbot.api.objects.Locatable;

public class Location extends com.github.manolo8.darkbot.core.utils.Location implements Locatable {

    public Location() {
    }

    public Location(double x, double y) {
        super(x, y);
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
