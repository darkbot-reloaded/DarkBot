package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.utils.Ammo;
import eu.darkbot.api.entities.utils.Attackable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AttackAPI extends API {

    boolean hasTarget();

    /**
     * @return currently set target
     */
    @Nullable Attackable getTarget();

    /**
     * @param entity to be set
     * @return previously set {@link Attackable}
     */
    @Nullable Attackable setTarget(@Nullable Attackable entity);

    /**
     * @return true if selected {@link Attackable} is targeted
     */
    boolean isTargeted();

    /**
     * Tries to select current target
     *
     * @return true if selection was successful
     * @see #getTarget()
     */
    boolean selectTarget();

    /**
     * @return true if {@link HeroAPI} is laser attacking selected target
     * @see #getTarget()
     */
    boolean isAttacking();

    /**
     * Tries to start laser attack.
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
     * @param laser ammo to be checked
     * @return true if given ammo can be handled via keyboard shortcut
     */
    boolean hasShortcut(@NotNull Ammo.Laser laser);
    boolean hasShortcut(@NotNull Ammo.Rocket rocket);

    /**
     * Overrides user settings to use given ammo.
     * Call {@code setLaserAmmo(null)} to use default ammo.
     * <p>
     * In case {@link Ammo.Laser#hasCooldown()} ammo,
     * will be used only when available/not in cooldown, otherwise will use the previous one.
     *
     * @param laserId to be set
     * @return previously set {@link Ammo.Laser}
     */
    @Nullable Ammo.Laser setLaserAmmo(@Nullable String laserId);
    @Nullable Ammo.Laser setLaserAmmo(@Nullable Ammo.Laser laserAmmo);

    /**
     * Overrides user settings to use given ammo.
     * Call {@code setRocketAmmo(null)} to use default ammo.
     *
     * @param rocketId to be set
     * @return previously set {@link Ammo.Rocket}
     */
    @Nullable Ammo.Rocket setRocketAmmo(@Nullable String rocketId);
    @Nullable Ammo.Rocket setRocketAmmo(@Nullable Ammo.Rocket rocketAmmo);
}
