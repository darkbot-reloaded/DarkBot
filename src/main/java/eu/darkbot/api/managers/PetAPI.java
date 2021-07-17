package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.game.entities.Pet;
import eu.darkbot.api.game.enums.PetGear;
import eu.darkbot.api.game.other.Location;
import eu.darkbot.api.utils.ItemNotEquippedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * API to manage hero's pet.
 * Pet is automatically repaired if {@link #isEnabled()} returns true,
 * user enabled Pet in settings, and the bot is running.
 */
public interface PetAPI extends Pet, API.Singleton {

    /**
     * @return true if pet was set to enabled by the current module via {@link #setEnabled}
     */
    boolean isEnabled();

    /**
     * Will set the flag for if pet is enabled or disabled to use by the current module.
     *
     * All modules should call this method to enable or disable pet depending on operation.
     * A module that doesn't call this method will see inconsistent PET behavior, it will be
     * enabled or disabled depending on what the previous module set it to.
     *
     * Regardless of if it's enabled, pet will not be used if the user hasn't enabled it in the settings.
     *
     * @param enabled true to enable pet, false to disable
     */
    void setEnabled(boolean enabled);

    /**
     * @return true if pet is active (alive and on the map).
     */
    boolean isActive();

    /**
     * @return true if pet is repaired.
     */
    boolean isRepaired();

    /**
     * @return amount of times the pet has been repaired
     */
    int getRepairCount();

    /**
     * @param gearId gear to check if available to set
     * @return true if the hero has this gear equipped and can be set, false otherwise.
     */
    boolean hasGear(int gearId);

    default boolean hasGear(@NotNull PetGear petGear) {
        return hasGear(petGear.getId());
    }

    /**
     * Sets the gear that the pet should be using, overriding what the user has asked for in the settings.
     *
     * This function must be called repeatedly for the gear to be set and maintained, if there are
     * no calls to setGear it will fall-back to the user-configured gear after a short period of time.
     *
     * This is done to avoid a permanent state change being done by a module that could be short-lived.
     * Only the module should call this function, as it's the one in control when conflicts can occur.
     *
     * @param gearId gear to set when possible, null to default back to user choice
     * @throws ItemNotEquippedException if given gear is not equipped or doesn't exist
     */
    void setGear(@Nullable Integer gearId) throws ItemNotEquippedException;

    default void setGear(@Nullable PetGear petGear) throws ItemNotEquippedException {
        setGear(petGear == null ? null : petGear.getId());
    }

    /**
     * Checks if pet has the given {@link PetGear.Cooldown},
     * which can make gears unavailable temporally.
     *
     * @param cooldownId to be checked
     * @return true if pet has given {@code cooldownId}
     * @see PetGear.Cooldown
     */
    boolean hasCooldown(int cooldownId);

    default boolean hasCooldown(@NotNull PetGear.Cooldown cooldown) {
        return hasCooldown(cooldown.getId());
    }

    /**
     * @param petGear to be checked
     * @return true if given gear is currently cooling down
     */
    default boolean hasCooldown(@NotNull PetGear petGear) {
        PetGear.Cooldown cd = petGear.getCooldown();
        return cd != null && hasCooldown(cd);
    }

    /**
     * @return {@link Location} of pet locator target ping, {@link Optional#empty()} if unavailable.
     */
    Optional<Location> getLocatorNpcLoc();

    /**
     * Get fuel, xp, and other pet stats available in the window
     *
     * @param stat PET stat to search for
     * @return PET stat corresponding to the asked type
     */
    PetStat getStat(Stat stat);

    /**
     * Represents a stat in the pet, like health, shield, fuel, etc
     */
    interface PetStat {
        /**
         * @return current value of this stat in the pet window
         */
        double getCurrent();

        /**
         * @return the maximum value of this stat in the pet window
         */
        double getTotal();
    }

    /**
     * The available pet stats for search
     */
    enum Stat {
        HP, SHIELD, FUEL, XP, HEAT;
    }

}
