package eu.darkbot.api.game.entities;

import eu.darkbot.api.game.other.Area;

/**
 * Generic in-game zone, like barriers or mist
 *
 * @see Barrier
 * @see Mist
 */
public interface Zone extends Entity {
    /**
     * @return {@link Area} of the {@link Zone}
     */
    Area getArea();

}
