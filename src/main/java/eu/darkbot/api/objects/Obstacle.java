package eu.darkbot.api.objects;

import eu.darkbot.api.entities.utils.Area;

/**
 * Represents in-game obstacle.
 */
public interface Obstacle {

    /**
     * @return {@link Area} of the {@link Obstacle}
     */
    Area getArea();

    boolean isValid();
}
