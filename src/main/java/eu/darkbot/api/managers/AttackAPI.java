package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.utils.Attackable;
import org.jetbrains.annotations.Nullable;

public interface AttackAPI extends API.Singleton {

    /**
     * @return true if target is non-null
     */
    default boolean hasTarget() {
        Attackable target = getTarget();
        return target != null && target.isValid();
    }

    /**
     * @return currently set target entity
     */
    @Nullable Attackable getTarget();

    /**
     * @param attackable The entity to attack, null to set none
     */
    void setTarget(@Nullable Attackable attackable);

    /**
     * This method checks if {@link #getTarget()} is locked/marked/targeted in-game.
     *
     * @return true if target is locked in-game
     */
    boolean isLocked();

    /**
     * @return true if {@link HeroAPI} is laser attacking selected target
     * @see #getTarget()
     */
    boolean isAttacking();

    /**
     * Will try to lock the entity and attack it. The method should be called every
     * tick that you want to keep on trying to attack the entity. It will keep on
     * trying to lock, and if locked, try to keep attacking.
     */
    void tryLockAndAttack();

    /**
     * Will stop the attack on the target. The method should be called every tick
     * to keep retrying in case of failure.
     */
    void stopAttack();

    /**
     * This method will give general guidelines to the module of what radius should
     * be used to circle this enemy with the current attacker. Essentially it will
     * apply transformations to the desired radius so that the attacker can function.
     *
     * Some use-cases considered:
     *  - When trying to lock, you must get a certain distance close enough to the target
     *  - When trying to cast an offensive ability, you need to get close enough
     *  - If the NPC isn't moving, radius has a maximum that no is no longer countered by latency
     *  - If using ship with modified range (eg: zephyr), this will dynamically add that radius too
     *  - If the user configured chasing NPCs with smaller radius, this applies that too
     *
     * @param radius The desired radius to circle the target by the module
     * @return the {@param radius} but modified to fit the needs of the attacker
     */
    double modifyRadius(double radius);

}
