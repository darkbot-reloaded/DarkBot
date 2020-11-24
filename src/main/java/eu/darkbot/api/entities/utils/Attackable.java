package eu.darkbot.api.entities.utils;

import eu.darkbot.api.entities.Entity;
import eu.darkbot.api.objects.Health;
import eu.darkbot.api.objects.Info;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Attackable extends Entity, Health, Info {

    /**
     * @return the target of this {@link Attackable} if present else {@code null}
     */
    @Nullable
    Entity getTarget();

    /**
     * Check lock type for this {@link Attackable}
     *
     * @return {@link Lock}
     */
    Lock getLockType();

    /**
     * @return true if attacks current target of this {@link Attackable}
     * @see #getTarget()
     */
    boolean isAttacking();

    /**
     * Returns true if ship aims other ship by checking theirs angle.
     *
     * @see #getAngle()
     */
    boolean isAiming(@NotNull Attackable other);

    /**
     * @return speed of the {@link Attackable} in-game.
     */
    int getSpeed();

    /**
     * @return angle of the {@link Attackable} in-game as radians.
     */
    double getAngle();

    /**
     * Represents lock types in-game.
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
         * Citadel's draw fire ability. ?
         */
        PURPLE,

        /**
         * ?
         */
        GRAY_DARK;

        public static Lock getType(int lockId) {
            if (lockId >= values().length || lockId < 0) return UNKNOWN;
            return values()[lockId];
        }
    }
}
