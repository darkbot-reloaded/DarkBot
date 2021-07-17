package eu.darkbot.api.game.enums;

import eu.darkbot.api.utils.EquippableItem;

/**
 * Represents pet gears which can be used by {@link eu.darkbot.api.managers.PetAPI}
 */
public enum PetGear implements EquippableItem {
    PASSIVE("Passive mode"),
    GUARD("Guard mode"),
    DESTROYER("Unknown id 3, Destroyer"),
    LOOTER("Auto-looter"),
    RESOURCE("Auto-resource collector"),
    ENEMY_LOCATOR("Enemy locator"),
    RESOURCE_LOCATOR("Resource locator"),
    TRADER("Cargo trader", Cooldown.TRADE),
    REPAIR("P.E.T. repairer"),
    KAMIKAZE("Kamikaze Detonator", Cooldown.KAMIKAZE),
    COMBO_REPAIR("Combo Ship Repair Gear", Cooldown.COMBO_REPAIR),
    COMBO_GUARD("Combo Guard Mode Gear"),
    DESTROYER_2("Unknown id 13, Destroyer"),
    SACRIFICIAL("Sacrificial Flame", Cooldown.FRIENDLY_SACRIFICE),
    PET_TARGET("Retargeting P.E.T. Gear", Cooldown.RETARGETING),
    HP_LINK("HP Link P.E.T. Gear", Cooldown.HP_LINK),
    MEGA_MINE("Mega-Mine Gear", Cooldown.MEGA_MINE),
    BEACON_COMBAT("Beacon-Combat Gear", Cooldown.BEACON_COMBAT),
    BEACON_HP("Beacon-HP Gear", Cooldown.BEACON_HP),
    UNKNOWN("Unknown Gear");

    private final String name;
    private final Cooldown cooldown;

    PetGear(String name) {
        this(name, null);
    }

    PetGear(String name, Cooldown cooldown) {
        this.name = name;
        this.cooldown = cooldown;
    }

    /**
     * Get a gear by its ID
     * @param gearId the in-game id of the gear
     * @return The gear with the corresponding id, or null if not found
     */
    public static PetGear of(int gearId) {
        if (gearId < 0 || gearId >= values().length) return null;
        return values()[gearId];
    }

    /**
     * @return the in-game id of this pet gear
     */
    public int getId() {
        return ordinal() + 1;
    }

    /**
     * @return A user-readable name for the gear
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @return The cooldown associated with this particular gear, null if no cooldown applies
     */
    public Cooldown getCooldown() {
        return cooldown;
    }

    /**
     * Represents cooldown of a {@link PetGear} in the {@link eu.darkbot.api.game.entities.Pet} buffs or debuffs.
     */
    public enum Cooldown {
        SINGULARITY,
        SPEED_LEECH,
        TRADE,
        WEAKEN_SHIELD,
        KAMIKAZE,
        COMBO_REPAIR,
        FRIENDLY_SACRIFICE,
        RETARGETING,
        HP_LINK,
        MEGA_MINE,
        BEACON_COMBAT,
        BEACON_HP;

        /**
         * @return The in-game id of this pet cooldown
         */
        public int getId() {
            return ordinal() + 1;
        }
    }
}
