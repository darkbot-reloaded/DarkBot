package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.config.types.suppliers.ReviveSpotSupplier;
import com.github.manolo8.darkbot.config.types.suppliers.ShipConfigSupplier;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.tree.components.JBoxInfoTable;
import com.github.manolo8.darkbot.gui.tree.components.JListField;
import com.github.manolo8.darkbot.gui.tree.components.JNpcInfoTable;
import com.github.manolo8.darkbot.gui.tree.components.JPercentField;

import java.util.HashMap;
import java.util.Map;

public class Config {

    public int WORKING_MAP = 26;

    public @Option("General") General GENERAL = new General();
    public static class General {
        public @Option("Offensive config")
        ShipConfig OFFENSIVE = new ShipConfig(1, '8');
        public @Option("Run config")
        ShipConfig RUN = new ShipConfig(2, '9');
        public @Option("Roam config")
        ShipConfig ROAM = new ShipConfig(1, '9');

        public @Option("Safety") Safety SAFETY = new Safety();
        public static class Safety {
            @Option("Run to repair at")
            @Editor(JPercentField.class)
            public double REPAIR_HP = 0.4;
            @Option("Run to repair when not killing npcs")
            @Editor(JPercentField.class)
            public double REPAIR_HP_NO_NPC = 0.5;
            @Option("Repair until")
            @Editor(JPercentField.class)
            public double REPAIR_TO_HP = 0.95;
            @Option(value = "Ship ability", description = "Clicked when running away")
            public Character SHIP_ABILITY;
            @Option("Max deaths")
            @Num(min = 1, max = 999)
            public int MAX_DEATHS = 10;
            @Option("Revive location")
            @Editor(JListField.class)
            @Options(ReviveSpotSupplier.class)
            public long REVIVE_LOCATION = 1L;
            @Option("Wait after revive (sec)")
            @Num(max = 60 * 60, step = 10)
            public int WAIT_AFTER_REVIVE = 90;
        }
    }

    public int CURRENT_MODULE;

    //LOOT MODULE
    public char AMMO_KEY = '3';
    public boolean AUTO_SAB = true;
    public char AUTO_SAB_KEY = '4';
    //LOOT MODULE

    //COLLECTOR MODULE
    public boolean STAY_AWAY_FROM_ENEMIES;
    public boolean AUTO_CLOACK;
    public char AUTO_CLOACK_KEY;
    //COLLECTOR MODULE

    // DEFINED AREAS
    public Map<Integer, ZoneInfo> AVOIDED = new HashMap<>();
    public Map<Integer, ZoneInfo> PREFERRED = new HashMap<>();
    // DEFINED AREAS

    public transient boolean changed;

    public transient Lazy<String> addedBox = new Lazy<>();
    public transient Lazy<String> addedNpc = new Lazy<>();

    public @Option("Collect") Collect COLLECT = new Collect();
    public static class Collect {
        @Option("Resources")
        @Editor(JBoxInfoTable.class)
        public Map<String, BoxInfo> BOX_INFOS = new HashMap<>();
    }

    public @Option("Loot") Loot LOOT = new Loot();
    public static class Loot {
        public @Option("Safety") Safety SAFETY = new Safety();
        public static class Safety {
            public @Option("Run from enemies")
            boolean RUN_FROM_ENEMIES = true;
            public @Option("Run from enemies in sight")
            boolean RUN_FROM_ENEMIES_SIGHT;
            @Option(value = "Stop running when out of sight", description = "Will stop running if the enemy isn't attacking and is no longer on sight")
            public boolean STOP_RUNNING_NO_SIGHT = true;
            @Option(value = "Max sight distance", description = "No longer consider enemies in sight if further away than this")
            @Num(min = 500, max = 20000, step = 500)
            public int MAX_SIGHT_DISTANCE = 4000;
        }

        @Option(value = "Run config when circling", description = "Use run config to follow escaping npcs")
        public boolean RUN_CONFIG_IN_CIRCLE = true;
        @Option(value = "Offensive ability key")
        public Character SHIP_ABILITY;
        @Option(value = "Offensive ability min health", description = "Min NPC health to use ability")
        @Num(min = 50_000, max = 5_000_000, step = 50_000)
        public int SHIP_ABILITY_MIN = 150_000;

        public @Option("Sab when under") @Editor(JPercentField.class) double SAB_PERCENT = 0.8;
        public @Option("Sab NPC min") @Num(min = 500, max = 100000, step = 500) int SAB_NPC_AMOUNT = 12000;

        @Option("Npcs")
        @Editor(JNpcInfoTable.class)
        public Map<String, NpcInfo> NPC_INFOS = new HashMap<>();
    }

    public @Option("Loot & collect") LootNCollect LOOT_COLLECT = new LootNCollect();
    public static class LootNCollect {
        @Option(value = "Collect radius", description = "Resource collection radius while killing NPCs")
        @Num(max = 10000, step = 50)
        public int RADIUS = 400;
    }

    public @Option("Event") Event EVENT = new Event();
    public static class Event {
        @Option(value = "Offensive ship ability")
        public Character SHIP_ABILITY;
        @Option(value = "Complete event progress", description = "If the bot should click on the event progress")
        public boolean PROGRESS = true;
    }


    public @Option("Pet") PetSettings PET = new PetSettings();
    public static class PetSettings {
        @Option("Use pet")
        public boolean ENABLED = true;
        @Option(value = "# of module to use", description = "0 -> Passive, 1 -> Guard module, then whatever's next")
        @Num(max = 8, step = 1)
        public int MODULE = 1;
    }

    public @Option("Miscellaneous") Miscellaneous MISCELLANEOUS = new Miscellaneous();
    public static class Miscellaneous {
        @Option(value = "Hide name", description = "Hide hero name in the map")
        public boolean HIDE_NAME;
        @Option(value = "Trail length", description = "Amount of time the trail should be in the map in seconds")
        @Num(max = 300, step = 1)
        public int TRAIL_LENGTH = 15;
        @Option(value = "Zone precision", description = "Amount of map subdivisions when selecting zones")
        @Num(min = 10, max = 300)
        public int ZONE_RESOLUTION = 30;
        @Option(value = "Show zones in main map", description = "Tick to show avoided/preferred zones on map")
        public boolean SHOW_ZONES = true;
        @Option(value = "Always on top", description = "Should the bot window stay on top of other windows?")
        public boolean ALWAYS_ON_TOP = true;
        @Option("Use darcula theme")
        public boolean USE_DARCULA_THEME = true;
        @Option(value = "Refresh every", description = "Every how many minutes to refresh")
        @Num(max = 60 * 12, step = 10)
        public int REFRESH_TIME = 0;

        @Option("Developer stuff shown")
        public boolean DEV_STUFF = false;
    }



    public static class ShipConfig {
        public ShipConfig() {}

        ShipConfig(int CONFIG, char FORMATION) {
            this.CONFIG = CONFIG;
            this.FORMATION = FORMATION;
        }

        @Option("Ship config")
        @Editor(JListField.class)
        @Options(ShipConfigSupplier.class)
        public int CONFIG = 1;
        public @Option("Formation key") char FORMATION = '9';
    }
}
