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
import com.github.manolo8.darkbot.utils.MathUtils;

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
        @Options(ModuleSupplier.class)
        public @Option @Editor(JListField.class) String CURRENT_MODULE = LootNCollectorModule.class.getCanonicalName();
        @Options(StarManager.MapList.class)
        public @Option @Editor(JListField.class) int WORKING_MAP = 26;
        public @Option ShipConfig OFFENSIVE = new ShipConfig(1, '8');
        public @Option ShipConfig ROAM = new ShipConfig(1, '9');
        public @Option ShipConfig RUN = new ShipConfig(2, '9');
        public @Option @Num(max = 3600) int FORMATION_CHECK = 180;

        public @Option Safety SAFETY = new Safety();
        public static class Safety {
            @Option(value = "Keep health between", description = "Even if shooting an npc, run away when under this hp")
            public PercentRange REPAIR_HP_RANGE = new PercentRange(0.4, 0.95);
            @Option(value = "Repair when roaming & hp under", description = "Hp to run at when just roaming the map")
            @Editor(JPercentField.class)
            public double REPAIR_HP_NO_NPC = 0.5;
            @Option(value = "Repair until shield", description = "Minimum shield to stop repairing")
            @Editor(JPercentField.class)
            public double REPAIR_TO_SHIELD = 1;
            @Option(value = "Repair config", description = "Used to repair after run formation shields are full")
            public ShipConfig REPAIR = new ShipConfig(1, '9');
            @Option(value = "Max deaths", description = "Max amount of deaths, if reached, bot is paused")
            @Num(min = 1, max = 9999)
            public int MAX_DEATHS = 10;
            @Option(value = "Revive location", description = "Where to revive the ship")
            @Editor(JListField.class)
            @Options(ReviveSpotSupplier.class)
            public long REVIVE_LOCATION = 1L;
            @Option(value = "Wait before revive (sec)", description = "Seconds to wait before reviving")
            @Num(min = 2, max = 60, step = 10)
            public int WAIT_BEFORE_REVIVE = 3;
            @Option(value = "Wait after revive (sec)", description = "Seconds to wait after reviving, lets ship repair")
            @Num(min = 3, max = 15 * 60, step = 10)
            public int WAIT_AFTER_REVIVE = 90;
        }

        public @Option Running RUNNING = new Running();
        public static class Running {
            @Option(value = "Run from enemies", description = "If bot should run from enemies who attack you, or have attacked you recently")
            public boolean RUN_FROM_ENEMIES = true;
            @Option(value = "Remember enemies for", description = "How long to run in sight from enemies that shot you")
            @Num(max = 24 * 60 * 60, step = 300)
            public int REMEMBER_ENEMIES_FOR = 300;
            @Option(value = "Run from enemies in sight", description = "Run from enemies as soon as they enter your minimap")
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

        public @Option Roaming ROAMING = new Roaming();
        public static class Roaming {
            public @Option boolean KEEP = true;
            public @Option boolean SEQUENTIAL = false;
            public @Option boolean ONLY_KILL_PREFERRED = false;
        }
    }

    public @Option Collect COLLECT = new Collect();
    public static class Collect {
        public @Option boolean STAY_AWAY_FROM_ENEMIES;
        public @Option boolean AUTO_CLOACK;
        public @Option Character AUTO_CLOACK_KEY;
        public @Option @Num(max = 10000, step = 50) int RADIUS = 400;

        @Option
        @Editor(value = JBoxInfoTable.class, shared = true)
        public Map<String, BoxInfo> BOX_INFOS = new HashMap<>();
        public transient Lazy<String> ADDED_BOX = new Lazy.NoCache<>();
    }

    public @Option Loot LOOT = new Loot();
    public static class Loot {
        public @Option Sab SAB = new Sab();
        public static class Sab {
            public @Option boolean ENABLED = false;
            public @Option Character KEY = '2';
            public @Option @Editor(JPercentField.class) double PERCENT = 0.8;
            public @Option @Num(min = 500, max = 100000, step = 500) int NPC_AMOUNT = 12000;
        }
        public @Option Character AMMO_KEY = '1';
        public @Option Character SHIP_ABILITY;
        public @Option @Num(min = 50_000, max = 5_000_000, step = 50_000) int SHIP_ABILITY_MIN = 150_000;
        public @Option @Num(max = 10, step = 1) int MAX_CIRCLE_ITERATIONS = 5;
        public @Option boolean RUN_CONFIG_IN_CIRCLE = true;

        public @Option boolean GROUP_NPCS = true;
        @Editor(value = JNpcInfoTable.class, shared = true)
        public @Option Map<String, NpcInfo> NPC_INFOS = new HashMap<>();
        public transient Lazy<String> MODIFIED_NPC = new Lazy.NoCache<>();

        public @Option @Num(min = 1000, max = 20000, step = 500) int NPC_DISTANCE_IGNORE = 3000;
    }

    public @Option PetSettings PET = new PetSettings();
    public static class PetSettings {
        public @Option boolean ENABLED = false;
        public @Option @Num(max = 8, step = 1) int MODULE = 1;
    }

    public @Option Miscellaneous MISCELLANEOUS = new Miscellaneous();
    public static class Miscellaneous {
        public @Option @Num(max = 60 * 12, step = 10) int REFRESH_TIME = 60;
        public @Option @Num(max = 60 * 12, step = 10) int PAUSE_FOR = 5;
        public @Option @Editor(JPercentField.class) double REPAIR_DRONE_PERCENTAGE = 0.9;
    }

    public @Option BotSettings BOT_SETTINGS = new BotSettings();
    public static class BotSettings {
        public @Option String lang = "en";
        public @Option @Num(min = 10, max = 300) int ZONE_RESOLUTION = 30;
        public @Option boolean MAP_START_STOP = false;
        public @Option boolean CONFIRM_EXIT = true;
        public @Option @Num(min = 10, max = 250) int MIN_TICK = 15;
        public @Option boolean DEV_STUFF = false;
        public /*@Option @Num(min = 0, max = 1, step = 1) */ int API = 0;

        public @Option Display DISPLAY = new Display();
        public static class Display {
            public @Option boolean SHOW_NAMES;
            public @Option boolean HIDE_NAME;
            public @Option @Num(max = 300, step = 1) int TRAIL_LENGTH = 15;
            public @Option boolean SHOW_ZONES = true;
            public @Option @Num(min = 1, max = 20, step = 1) int BUTTON_SIZE = 4;
            public @Option boolean HIDE_EDITORS = false;

            public boolean ALWAYS_ON_TOP = true; // No @Option. Edited via button
        }
    }

    public /*@Option("Extra actions")*/ ExtraActions EXTRA = new ExtraActions();
    public static class ExtraActions {
        @Option
        @Editor(value = JActionTable.class, shared = true)
        public Map<String, ActionInfo> ACTION_INFOS = new HashMap<>();
        public transient Lazy<String> MODIFIED_ACTIONS = new Lazy.NoCache<>();
    }

    public static class ShipConfig {
        public int CONFIG = 1;
        public Character FORMATION;

        public ShipConfig() {}
        public ShipConfig(int CONFIG, Character FORMATION) {
            this.CONFIG = CONFIG;
            this.FORMATION = FORMATION;
        }

        @Override
        public String toString() {
            return "Config: " + CONFIG + "   Formation: " + Objects.toString(FORMATION, "(unset)");
        }
    }

    public static class PercentRange {
        public double min, max;

        public PercentRange() {}
        public PercentRange(double min, double max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public String toString() {
            return Math.round(min * 100) + "%-" + Math.round(max * 100) + "%";
        }
    }
}
