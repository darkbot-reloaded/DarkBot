package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.config.types.Options;
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
    }

    public int CURRENT_MODULE;

    public int MAX_DEATHS = 10;

    public int REPAIR_LOCAL = 0;

    public double REPAIR_HP;
    public double WAIT_HP;

    //ENTITIES
    public HashMap<String, BoxInfo> boxInfos = new HashMap<>();
    public HashMap<String, NpcInfo> npcInfos = new HashMap<>();
    //ENTITIES

    //LOOT MODULE
    public boolean RUN_FROM_ENEMIES;
    public boolean RUN_FROM_ENEMIES_IN_SIGHT;
    public char AMMO_KEY = '3';
    public boolean AUTO_SAB = true;
    public char AUTO_SAB_KEY = '4';
    //LOOT MODULE

    //COLLECTOR MODULE
    public boolean STAY_AWAY_FROM_ENEMIES;
    public boolean AUTO_CLOACK;
    public char AUTO_CLOACK_KEY;
    //COLLECTOR MODULE

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
        @Option(value = "Max sight distance", description = "No longer consider enemies in sight if further away than this")
        @Num(min = 500, max = 20000, step = 500)
        public int MAX_SIGHT_DISTANCE = 4000;

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
        @Option(value = "Always on top", description = "Should the bot window stay on top of other windows?")
        public boolean ALWAYS_ON_TOP = true;
        @Option("Use darcula theme")
        public boolean USE_DARCULA_THEME = true;
        @Option(value = "Refresh every", description = "Every how many minutes to refresh")
        @Num(max = 60 * 12, step = 10)
        public int REFRESH_TIME = 0;
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
