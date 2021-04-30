package eu.darkbot.api.entities.utils;

import eu.darkbot.api.entities.Entity;
import eu.darkbot.api.objects.EntityInfo;
import eu.darkbot.api.objects.Health;

/**
 * An entity that can be locked & attacked
 */
public interface Attackable extends Entity {

    /**
     * Check lock type for this {@link Attackable}
     *
     * @return {@link Lock}
     */
    Lock getLockType();

    /**
     * @return The health representation of this attackable
     */
    Health getHealth();

    /**
     * @return The info representation of this attackable
     */
    EntityInfo getEntityInfo();

    /**
     * In-game lock on the selected entity
     */
    enum Lock {

        /**
         * Unknown
         */
        UNKNOWN,

        /**
         * Owned by {@link eu.darkbot.api.managers.HeroAPI}.
         */
        RED,

        /**
         * Owned by someone else.
         */
        GRAY_LIGHT,

        /**
         * Citadel's draw fire ability.
         */
        PURPLE,

        /**
         * ?
         */
        GRAY_DARK;

        public static Lock of(int lockId) {
            if (lockId >= values().length || lockId < 0) return UNKNOWN;
            return values()[lockId];
        }
    }

}
