package eu.darkbot.api.entities;

import eu.darkbot.api.entities.utils.Area;

/**
 * Default zone entity in darkorbit.
 *
 * @see Barrier
 * @see Mist
 */
public interface Zone extends Entity {
    /**
     * @return {@link Area} of that Zone
     */
    Area getArea();

    /**
     * Returns true if zone isn't outside map
     */
    boolean isZoneValid();
}
