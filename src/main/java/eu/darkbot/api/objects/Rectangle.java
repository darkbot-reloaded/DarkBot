package eu.darkbot.api.objects;

import eu.darkbot.api.entities.utils.Area;

public interface Rectangle extends Area, Locatable {

    double getWidth();
    double getHeight();

    double getX2();
    double getY2();

    /**
     * {@inheritDoc}
     */
    @Override
    default Rectangle getBounds() {
        return this;
    }
}
