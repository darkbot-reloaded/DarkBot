package eu.darkbot.api.objects;

import eu.darkbot.api.entities.Entity;
import eu.darkbot.api.entities.utils.Area;

/**
 * Represents in-game obstacle.
 */
public interface Obstacle extends Entity {

    /**
     * @return {@link Area} of the {@link Obstacle}
     */
    Area getArea();

    boolean isValid();
}
