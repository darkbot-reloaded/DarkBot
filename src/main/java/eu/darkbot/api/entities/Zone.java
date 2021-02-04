package eu.darkbot.api.entities;

import eu.darkbot.api.entities.utils.Area;

/**
 * Default zone entity in-game.
 *
 * @see Barrier
 * @see Mist
 */
public interface Zone extends Entity {
    /**
     * @return {@link Area} of the {@link Zone}
     */
    Area getArea();

    /**
     * Returns true if zone isn't outside map
     */
    boolean isZoneValid();
}
