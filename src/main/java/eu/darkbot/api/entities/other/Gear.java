package eu.darkbot.api.entities.other;

import eu.darkbot.api.utils.EquippableItem;

/**
 * Represents pet gears which can be used by {@link eu.darkbot.api.managers.PetAPI}
 */
public enum Gear implements EquippableItem {
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
    BEACON_HP("Beacon-HP Gear", Cooldown.BEACON_HP);

    private final String name;
    private final Cooldown cooldown;

    Gear(String name) {
        this(name, null);
    }

    Gear(String name, Cooldown cooldown) {
        this.name = name;
        this.cooldown = cooldown;
    }

    public static String getName(Integer id) {
        if (id == null) return null;
        if (Gear.values().length > id) return Gear.values()[id - 1].name;
        return "Unknown gear " + id;
    }

    public int getId() {
        return ordinal() + 1;
    }

    public Cooldown getCooldown() {
        return cooldown;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Represents cooldowns of {@link Gear}, {@link eu.darkbot.api.entities.Pet} buffs/debuffs etc.
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

        public int getId() {
            return ordinal() + 1;
        }
    }
}
