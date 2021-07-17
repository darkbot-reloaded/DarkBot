package eu.darkbot.api.game.other;

import eu.darkbot.api.game.entities.Entity;

/**
 * Generic obstacle implementation.
 * This may be an in-game unavoidable obstacle, or other things like
 * enemy battle stations, mines or user-defined zones.
 */
public interface Obstacle extends Entity {

    /**
     * @return {@link Area} that this {@link Obstacle} occupies
     */
    Area getArea();

    /**
     * @return true if the obstacle is still valid and in the map, false if it should be discarded
     */
    boolean isValid();

    /**
     * If the obstacle should be used in pathfinding, or can be ignored.
     *
     * Example: battle station is only a obstacle if it's enemy
     *          (no need to avoid friendly stations) and you aren't invisible.
     *
     * @return If this obstacle should be used currently
     */
    boolean use();
}
