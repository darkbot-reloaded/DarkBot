package eu.darkbot.utils;

import eu.darkbot.api.objects.Locatable;

public class Location extends com.github.manolo8.darkbot.core.utils.Location implements Locatable {

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public Location getLocation() {
        return this;
    }
}
