package eu.darkbot.api.entities;

import eu.darkbot.api.entities.utils.Area;

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
