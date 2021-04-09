package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.other.SelectableItem;
import eu.darkbot.api.entities.utils.Attackable;
import org.jetbrains.annotations.Nullable;

public interface AttackAPI extends API.Singleton {

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
     * @param attackable to be set
     */
    void setTarget(@Nullable Attackable attackable);

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
     */
    void laserAttack();

    /**
     * Tries to stop laser attack.
     */
    void laserAbort();

    /**
     * @return currently used {@link SelectableItem.Laser}
     //* @see #setLaser(Ammo.Laser)
     */
    @Nullable SelectableItem.Laser getLaser();

    /**
     * Tries to set {@link SelectableItem.Laser} ammunition.
     *
     * @param laser to be set
     * @return true if laser is available and successfully/already set
     * @see #getLaser()
     */
    //boolean setLaser(@Nullable Ammo.Laser laser);

    /**
     * Tries to launch rocket.
     * @see #getRocket()
     */
    void launchRocket();

    /**
     * @return currently used {@link SelectableItem.Rocket}
     //* @see #setRocket(Ammo.Rocket)
     */
    @Nullable SelectableItem.Rocket getRocket();

    /**
     * Tries to set {@link SelectableItem.Rocket} ammunition.
     *
     * @param rocket to be set
     * @return true if rocket is available and successfully/already set.
     * @see #getRocket()
     * @see #launchRocket()
     */
    //boolean setRocket(@Nullable Ammo.Rocket rocket);
}
