package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.config.types.suppliers.ModuleSupplier;
import com.github.manolo8.darkbot.config.types.suppliers.ReviveSpotSupplier;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.tree.components.JActionTable;
import com.github.manolo8.darkbot.gui.tree.components.JBoxInfoTable;
import com.github.manolo8.darkbot.gui.tree.components.JListField;
import com.github.manolo8.darkbot.gui.tree.components.JNpcInfoTable;
import com.github.manolo8.darkbot.gui.tree.components.JPercentField;
import com.github.manolo8.darkbot.modules.LootNCollectorModule;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Config {

    // DEFINED AREAS
    public Map<Integer, ZoneInfo> AVOIDED = new HashMap<>();
    public Map<Integer, ZoneInfo> PREFERRED = new HashMap<>();
    public Map<Integer, Set<SafetyInfo>> SAFETY = new HashMap<>();
    public transient Lazy<SafetyInfo> ADDED_SAFETY = new Lazy.NoCache<>();
    // DEFINED AREAS

    public Map<String, Object> CUSTOM_CONFIGS = new HashMap<>();

    public Map<String, PluginInfo> PLUGIN_INFOS = new HashMap<>();

    public transient boolean changed;

    public @Option("General") General GENERAL = new General();
    public static class General {
        @Option("Module")
        @Editor(JListField.class)
        @Options(ModuleSupplier.class)
        public String CURRENT_MODULE = LootNCollectorModule.class.getCanonicalName();

        @Option("Working map")
        @Editor(JListField.class)
        @Options(StarManager.MapList.class)
        public int WORKING_MAP = 26;
        @Option(value = "Offensive config", description = "Used to kill NPCs")
        public ShipConfig OFFENSIVE = new ShipConfig(1, '8');
        @Option(value = "Roam config", description = "Used to roam around the map, searching for NPCs")
        public ShipConfig ROAM = new ShipConfig(1, '9');
        @Option(value = "Run config", description = "Used to run to safety or switch around maps")
        public ShipConfig RUN = new ShipConfig(2, '9');

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
            @Option("Repair until shield")
            @Editor(JPercentField.class)
            public double REPAIR_TO_SHIELD = 1;
            @Option(value = "Repair config", description = "Used to repair after run formation shields are full")
            public ShipConfig REPAIR = new ShipConfig(1, '9');
            @Option("Max deaths")
            @Num(min = 1, max = 9999)
            public int MAX_DEATHS = 10;
            @Option("Revive location")
            @Editor(JListField.class)
            @Options(ReviveSpotSupplier.class)
            public long REVIVE_LOCATION = 1L;
            @Option("Wait before revive (sec)")
            @Num(min = 2, max = 60, step = 10)
            public int WAIT_BEFORE_REVIVE = 3;
            @Option("Wait after revive (sec)")
            @Num(min = 3, max = 15 * 60, step = 10)
            public int WAIT_AFTER_REVIVE = 90;
        }

        public @Option("Running") Running RUNNING = new Running();
        public static class Running {
            @Option("Run from enemies")
            public boolean RUN_FROM_ENEMIES = true;
            @Option(value = "Remember enemies for", description = "How long to run in sight from enemies that shot you")
            @Num(max = 24 * 60 * 60, step = 300)
            public int REMEMBER_ENEMIES_FOR = 300;
            @Option("Run from enemies in sight")
            public boolean RUN_FROM_ENEMIES_SIGHT = false;
            @Option(value = "Stop running when out of sight", description = "Will stop running if the enemy isn't attacking and is no longer on sight")
            public boolean STOP_RUNNING_NO_SIGHT = true;
            @Option(value = "Max sight distance", description = "No longer consider enemies in sight if further away than this")
            @Num(min = 500, max = 20000, step = 500)
            public int MAX_SIGHT_DISTANCE = 4000;
            @Option(value = "Ship ability", description = "Clicked when running away")
            public Character SHIP_ABILITY;
            @Option(value = "Ship ability min distance", description = "Minimum distance to safety to use ability")
            @Num(max = 20000, step = 500)
            public int SHIP_ABILITY_MIN = 1500;
            @Option(value = "Closest port distance max", description = "Run to port further away from enemy, unless port dist under max")
            @Num(max = 20000, step = 500)
            public int RUN_FURTHEST_PORT = 1500;
        }

        public @Option("Roaming & Preferred area behaviour") Roaming ROAMING = new Roaming();
        public static class Roaming {
            @Option("Keep roaming towards same point until reached")
            public boolean KEEP = false;
            @Option("Roam to preferred zones in order")
            public boolean SEQUENTIAL = false;
            @Option(value = "Only kill NPCs in preferred area", description = "Only select and kill NPCs when they are inside a preferred area")
            public boolean ONLY_KILL_PREFERRED = false;
        }
    }

    public @Option("Collect") Collect COLLECT = new Collect();
    public static class Collect {
        public @Option("Stay away from enemies") boolean STAY_AWAY_FROM_ENEMIES;
        public @Option("Auto cloack") boolean AUTO_CLOACK;
        public @Option("Auto cloack key") Character AUTO_CLOACK_KEY;

        @Option(value = "Collect while killing radius", description = "Resource collection radius while killing NPCs")
        @Num(max = 10000, step = 50)
        public int RADIUS = 400;

        @Option()
        @Editor(value = JBoxInfoTable.class, shared = true)
        public Map<String, BoxInfo> BOX_INFOS = new HashMap<>();
        public transient Lazy<String> ADDED_BOX = new Lazy.NoCache<>();
    }

    public @Option("Npc killer") Loot LOOT = new Loot();
    public static class Loot {
        public @Option(value = "Sab", description = "Auto sab npcs to survive longer") Sab SAB = new Sab();
        public static class Sab {
            public @Option("Enabled") boolean ENABLED = false;
            public @Option("Key") Character KEY = '2';
            public @Option("Ship under") @Editor(JPercentField.class) double PERCENT = 0.8;
            public @Option("NPC min shield") @Num(min = 500, max = 100000, step = 500) int NPC_AMOUNT = 12000;
        }
        public @Option("Ammo key") Character AMMO_KEY = '1';

        @Option(value = "Offensive ability key")
        public Character SHIP_ABILITY;
        @Option(value = "Offensive ability min health", description = "Min NPC health to use ability")
        @Num(min = 50_000, max = 5_000_000, step = 50_000)
        public int SHIP_ABILITY_MIN = 150_000;
        @Option(value = "Smart circling iterations", description = "Max moves to look ahead to change circle direction")
        @Num(max = 50, step = 1)
        public int MAX_CIRCLE_ITERATIONS = 5;
        @Option(value = "Run config to chase", description = "Use run config to follow escaping npcs")
        public boolean RUN_CONFIG_IN_CIRCLE = true;

        @Option(value = "Group similar NPCs", description = "Group NPCs in the same GG in the NPC table")
        public boolean GROUP_NPCS = true;
        @Option()
        @Editor(value = JNpcInfoTable.class, shared = true)
        public Map<String, NpcInfo> NPC_INFOS = new HashMap<>();
        public transient Lazy<String> MODIFIED_NPC = new Lazy.NoCache<>();

        @Option("Ignore npcs further than")
        @Num(min = 1000, max = 20000, step = 500)
        public int NPC_DISTANCE_IGNORE = 3000;
    }

    public @Option("Pet") PetSettings PET = new PetSettings();
    public static class PetSettings {
        @Option("Use pet")
        public boolean ENABLED = false;
        @Option(value = "# of module to use", description = "0 -> Passive, 1 -> Guard module, then whatever's next")
        @Num(max = 8, step = 1)
        public int MODULE = 1;
    }

    public @Option("Miscellaneous") Miscellaneous MISCELLANEOUS = new Miscellaneous();
    public static class Miscellaneous {
        public @Option("Display") Display DISPLAY = new Display();
        public static class Display {
            @Option(value = "Show usernames", description = "Show usernames on minimap")
            public boolean SHOW_NAMES;
            @Option(value = "Hide name", description = "Hide hero name in the map")
            public boolean HIDE_NAME;
            @Option(value = "Trail length", description = "Amount of time the trail should be in the map in seconds")
            @Num(max = 300, step = 1)
            public int TRAIL_LENGTH = 15;
            @Option(value = "Show zones in main map", description = "Tick to show avoided/preferred zones on map")
            public boolean SHOW_ZONES = true;
            @Option(value = "GUI Button size", description = "Change tab in config & resize main window to update.")
            @Num(min = 1, max = 20, step = 1)
            public int BUTTON_SIZE = 4;
            @Option(value = "Hide config editors until click", description = "Use text-based config tree, which is less resource intensive")
            public boolean HIDE_EDITORS = false;

            public boolean ALWAYS_ON_TOP = true; // No @Option. Edited via button
        }
        @Option(value = "Zone precision", description = "Amount of map subdivisions when selecting zones")
        @Num(min = 10, max = 300)
        public int ZONE_RESOLUTION = 30;
        @Option(value = "Refresh every", description = "Every how many minutes to refresh")
        @Num(max = 60 * 12, step = 10)
        public int REFRESH_TIME = 0;
        @Option("Repair Drone Percentage")
        @Editor(JPercentField.class)
        public double REPAIR_DRONE_PERCENTAGE = 0.9;
        @Option(value = "Start/pause clicking map", description = "Left click to start/pause. Right click to move ship.")
        public boolean MAP_START_STOP = false;
        @Option(value = "Confirm exiting", description = "Confirm before exiting the bot")
        public boolean CONFIRM_EXIT = true;
        @Option(value = "Minimum tick time")
        @Num(min = 10, max = 250)
        public int MIN_TICK = 15;
        @Option(value = "Developer stuff shown", description = "Enabling this WILL make your bot use more cpu.")
        public boolean DEV_STUFF = false;
    }

    public /*@Option("Extra actions")*/ ExtraActions EXTRA = new ExtraActions();
    public static class ExtraActions {
        @Option()
        @Editor(value = JActionTable.class, shared = true)
        public Map<String, ActionInfo> ACTION_INFOS = new HashMap<>();
        public transient Lazy<String> MODIFIED_ACTIONS = new Lazy.NoCache<>();
    }

    public static class ShipConfig {
        public ShipConfig() {}

        ShipConfig(int CONFIG, Character FORMATION) {
            this.CONFIG = CONFIG;
            this.FORMATION = FORMATION;
        }

        public int CONFIG = 1;
        public Character FORMATION;

        @Override
        public String toString() {
            return "Config: " + CONFIG + "   Formation: " + Objects.toString(FORMATION, "(unset)");
        }
    }
}
