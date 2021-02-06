package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.Pet;
import eu.darkbot.api.entities.other.Gear;
import eu.darkbot.api.objects.Location;
import eu.darkbot.api.utils.ItemNotEquippedException;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * API to manage hero's pet.
 */
public interface PetAPI extends Pet, API {

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

    void tryRepair();

    int getRepairCount();

    /**
     * @param gearId to be checked
     * @return true if given gear is available.
     */
    boolean isGearAvailable(int gearId);

    default boolean isGearAvailable(@NotNull Gear gear) {
        return isGearAvailable(gear.getId());
    }

    /**
     * @param gearId to be set
     * @throws ItemNotEquippedException if given gear is not equipped or doesn't exists
     */
    void setGear(int gearId) throws ItemNotEquippedException;

    default void setGear(@NotNull Gear gear) throws ItemNotEquippedException {
        setGear(gear.getId());
    }

    /**
     * @return {@link Location} of pet locator target
     * or else {@link Optional#empty()} if theres none.
     */
    Optional<Location> getLocatorNpcLoc();

    /**
     * Checks if pet has the given {@link Gear.Cooldown},
     * which can make gears unavailable temporally.
     *
     * @param cooldownId to be checked
     * @return true if pet has given {@code cooldownId}
     * @see Gear.Cooldown
     */
    boolean hasCooldown(int cooldownId);

    default boolean hasCooldown(@NotNull Gear.Cooldown cooldown) {
        return hasCooldown(cooldown.getId());
    }

    /**
     * @param gear to be checked
     * @return true if given gear is currently cooling down
     */
    default boolean hasCooldown(@NotNull Gear gear) {
        Gear.Cooldown cooldown = gear.getCooldown();
        return cooldown != null && hasCooldown(cooldown);
    }

    int getFuel();
    int getMaxFuel();

    int getHeat();
    int getMaxHeat();

}
