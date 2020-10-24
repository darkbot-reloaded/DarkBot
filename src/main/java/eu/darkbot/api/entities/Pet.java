package eu.darkbot.api.entities;

import eu.darkbot.utils.ThrowableItem;

public interface Pet extends Ship {

    int getLevel();
    int getOwnerId();

    enum Buff {
        SINGULARITY,
        SPEED_LEECH,
        TRADE,
        WEAKEN_SHIELD,
        KAMIKAZE_CD,
        COMBO_REPAIR_CD,
        FRIENDLY_SACRIFICE,
        RETARGETING_CD,
        HP_LINK_CD,
        MEGA_MINE_CD;

        public int getId() {
            return ordinal() + 1;
        }
    }

    enum Gear implements ThrowableItem {
        PASSIVE("Passive mode"),
        GUARD("Guard mode"),
        DESTROYER("Unknown id 3, Destroyer"),
        LOOTER("Auto-looter"),
        RESOURCE("Auto-resource collector"),
        ENEMY_LOCATOR("Enemy locator"),
        RESOURCE_LOCATOR("Resource locator"),
        TRADER("Cargo trader"),
        REPAIR("P.E.T. repairer"),
        KAMIKAZE("Kamikaze Detonator"),
        COMBO_REPAIR("Combo Ship Repair Gear"),
        COMBO_GUARD("Combo Guard Mode Gear"),
        DESTROYER_2("Unknown id 13, Destroyer"),
        SACRIFICIAL("Sacrificial Flame"),
        PET_TARGET("Retargeting P.E.T. Gear"),
        HP_LINK("HP Link P.E.T. Gear"),
        MEGA_MINE("Mega-Mine Gear"),
        BEACON_COMBAT("Beacon-Combat Gear"),
        BEACON_HP("Beacon-HP Gear");

        private final String name;

        Gear(String name) {
            this.name = name;
        }

        public static String getName(Integer id) {
            if (id == null) return null;
            if (Gear.values().length > id) return Gear.values()[id - 1].name;
            return "Unknown gear " + id;
        }

        public int getId() {
            return ordinal() + 1;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public String getMessage() {
            return "Gear: " + this + " is not equipped!";
        }
    }
}
