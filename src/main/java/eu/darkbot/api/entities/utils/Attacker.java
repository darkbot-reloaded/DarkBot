package eu.darkbot.api.entities.utils;

import eu.darkbot.api.entities.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Represents in-game entity which can attack other in-game entities.
 */
public interface Attacker extends Attackable {

    /**
     * @return the target if present, otherwise {@code null}
     */
    @Nullable
    Entity getTarget();

    /**
     * @return true if attacks current target
     * @see #getTarget()
     */
    boolean isAttacking();

    /**
     * @param other {@link Attackable} to check
     * @return true if attacks {@code other}
     */
    boolean isAttacking(Attackable other);
}
