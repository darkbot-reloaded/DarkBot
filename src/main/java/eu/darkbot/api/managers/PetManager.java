package eu.darkbot.api.managers;

import eu.darkbot.api.entities.Pet;
import eu.darkbot.api.objects.Gui;
import eu.darkbot.utils.ItemNotEquippedException;

public interface PetManager extends Gui, Pet {

    boolean isGearAvailable(int gearId);

    default boolean isGearAvailable(Pet.Gear gear) {
        return isGearAvailable(gear.getId());
    }

    void setGear(int gearId) throws ItemNotEquippedException;

    default void setGear(Pet.Gear gear) throws ItemNotEquippedException {
        setGear(gear.getId());
    }

    boolean hasCooldown(int cooldownId);

    default boolean hasCooldown(Pet.Cooldown cooldown) {
        return hasCooldown(cooldown.getId());
    }

    boolean isEnabled();
    void setEnabled(boolean enabled);

    boolean isActive();
    boolean isRepaired();

    int getFuel();
    int getMaxFuel();

    int getHeat();
    int getMaxHeat();

    @Override
    default int getHull() {
        return 0;
    }

    @Override
    default int getMaxHull() {
        return 0;
    }

    @Override
    default boolean hullDecreasedIn(int time) {
       return false;
    }

    @Override
    default boolean hullIncreasedIn(int time) {
        return false;
    }
}
