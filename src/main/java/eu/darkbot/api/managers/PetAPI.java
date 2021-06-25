package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.Pet;
import eu.darkbot.api.entities.other.PetGear;
import eu.darkbot.api.objects.Location;
import eu.darkbot.api.utils.ItemNotEquippedException;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * API to manage hero's pet.
 * Pet is automatically repaired if {@link #isEnabled()} returns true,
 * user enabled Pet in settings, and the bot is running.
 */
public interface PetAPI extends Pet, API.Singleton {

    /**
     * @return true if pet by module was marked to be enabled.
     */
    boolean isEnabled();

    /**
     * Will change enabled flag for pet.
     * If was set to {@code true} bot will try to enable the pet,
     * only if user configured bot to use the pet.
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
     * @return pet's repair count
     */
    int getRepairCount();

    /**
     * @param gearId to be checked
     * @return true if given gear is available.
     */
    boolean hasGear(int gearId);

    default boolean hasGear(@NotNull PetGear petGear) {
        return hasGear(petGear.getId());
    }

    /**
     * @param gearId to be set
     * @throws ItemNotEquippedException if given gear is not equipped or doesn't exists
     */
    void setGear(int gearId) throws ItemNotEquippedException;

    default void setGear(@NotNull PetGear petGear) throws ItemNotEquippedException {
        setGear(petGear.getId());
    }

    /**
     * @return {@link Location} of pet locator target
     * or else {@link Optional#empty()} if theres none.
     */
    Optional<Location> getLocatorNpcLoc();

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

    double getFuel();
    double getMaxFuel();

    double getHeat();
    double getMaxHeat();

}
