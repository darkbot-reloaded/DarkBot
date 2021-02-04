package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.other.Ammo;
import eu.darkbot.api.entities.utils.Attackable;
import org.jetbrains.annotations.Nullable;

public interface AttackAPI extends API {

    /**
     * @return true if target is non-null
     */
    default boolean hasTarget() {
        return getTarget() != null;
    }

    /**
     * @return currently set target
     */
    @Nullable Attackable getTarget();

    /**
     * Setting attackable to null stops attacking and removes target.
     *
     * @param attackable to be set
     * @return previously set {@link Attackable}
     */
    @Nullable Attackable setTarget(@Nullable Attackable attackable);

    /**
     * This method checks if {@link #getTarget()} is locked/marked/targeted in-game.
     *
     * @return true if target is locked in-game
     */
    boolean isLocked();

    /**
     * Tries to lock target in-game.
     */
    void tryLockTarget();

    /**
     * @return true if {@link HeroAPI} is laser attacking selected target
     * @see #getTarget()
     */
    boolean isAttacking();

    /**
     * This method will try to attack {@link #getTarget()}
     * Target doesn't need to be locked, this method will handle that.
     *
     * @return true on successful try
     */
    boolean laserAttack();

    /**
     * Tries to stop laser attack.
     *
     * @return true on successful try
     */
    boolean laserAbort();

    /**
     * @return currently used {@link Ammo.Laser}
     * @see #setLaser(Ammo.Laser)
     */
    @Nullable Ammo.Laser getLaser();

    /**
     * Tries to set {@link Ammo.Laser} ammunition.
     *
     * @param laser to be set
     * @return true if laser is available and successfully/already set
     * @see #getLaser()
     */
    boolean setLaser(@Nullable Ammo.Laser laser);

    /**
     * Tries to launch rocket.
     * @see #getRocket()
     */
    void launchRocket();

    /**
     * @return currently used {@link Ammo.Rocket}
     * @see #setRocket(Ammo.Rocket)
     */
    @Nullable Ammo.Rocket getRocket();

    /**
     * Tries to set {@link Ammo.Rocket} ammunition.
     *
     * @param rocket to be set
     * @return true if rocket is available and successfully/already set.
     * @see #getRocket()
     * @see #launchRocket()
     */
    boolean setRocket(@Nullable Ammo.Rocket rocket);
}
