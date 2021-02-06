package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.config.types.Tag;
import com.github.manolo8.darkbot.config.types.TagDefault;
import com.github.manolo8.darkbot.config.types.suppliers.ApiSupplier;
import com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag;
import com.github.manolo8.darkbot.config.types.suppliers.LanguageSupplier;
import com.github.manolo8.darkbot.config.types.suppliers.ModuleSupplier;
import com.github.manolo8.darkbot.config.types.suppliers.PetGearSupplier;
import com.github.manolo8.darkbot.config.types.suppliers.ReviveSpotSupplier;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.tree.components.JActionTable;
import com.github.manolo8.darkbot.gui.tree.components.JBoxInfoTable;
import com.github.manolo8.darkbot.gui.tree.components.JCharField;
import com.github.manolo8.darkbot.gui.tree.components.JCheckboxListField;
import com.github.manolo8.darkbot.gui.tree.components.JListField;
import com.github.manolo8.darkbot.gui.tree.components.JNpcInfoTable;
import com.github.manolo8.darkbot.gui.tree.components.JPercentField;
import com.github.manolo8.darkbot.gui.tree.components.LangEditor;
import com.github.manolo8.darkbot.modules.LootNCollectorModule;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag.*;

public class Config {

    // Defined map areas
    public Map<Integer, ZoneInfo> AVOIDED = new HashMap<>();
    public Map<Integer, ZoneInfo> PREFERRED = new HashMap<>();
    public Map<Integer, Set<SafetyInfo>> SAFETY = new HashMap<>();
    public transient Lazy<SafetyInfo> ADDED_SAFETY = new Lazy.NoCache<>();

    // Player list & resolver
    public Map<Integer, PlayerInfo> PLAYER_INFOS = new HashMap<>();
    public Set<PlayerTag> PLAYER_TAGS = new HashSet<>();
    public transient Lazy<Integer> PLAYER_UPDATED = new Lazy.NoCache<>();
    public Queue<UnresolvedPlayer> UNRESOLVED = new LinkedList<>();


    // Plugin custom configuration objects
    public Map<String, Object> CUSTOM_CONFIGS = new HashMap<>();
    public Map<String, PluginConfig> PLUGIN_INFOS = new HashMap<>();

    public transient boolean changed;

    public @Option General GENERAL = new General();
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
            public @Option PercentRange REPAIR_HP_RANGE = new PercentRange(0.4, 0.95);
            public @Option @Editor(JPercentField.class) double REPAIR_HP_NO_NPC = 0.5;
            public @Option @Editor(JPercentField.class) double REPAIR_TO_SHIELD = 1;
            public @Option ShipConfig REPAIR = new ShipConfig(1, '9');
            public @Option @Num(min = 1, max = 9999) int MAX_DEATHS = 10;
            public @Option @Editor(JListField.class) @Options(ReviveSpotSupplier.class) long REVIVE_LOCATION = 1L;
            public @Option @Num(min = 5, max = 60, step = 10) int WAIT_BEFORE_REVIVE = 5;
            public @Option @Num(min = 3, max = 15 * 60, step = 10) int WAIT_AFTER_REVIVE = 90;
        }

        public @Option Running RUNNING = new Running();
        public static class Running {
            public @Option boolean RUN_FROM_ENEMIES = true;
            public @Option @Num(max = 24 * 60 * 60, step = 300) int REMEMBER_ENEMIES_FOR = 300;
            public @Option boolean RUN_FROM_ENEMIES_SIGHT = false;
            public @Option boolean STOP_RUNNING_NO_SIGHT = true;
            public @Option @Num(min = 500, max = 20000, step = 500) int MAX_SIGHT_DISTANCE = 4000;
            public @Option Character SHIP_ABILITY;
            public @Option @Num(max = 20000, step = 500) int SHIP_ABILITY_MIN = 1500;
            public @Option @Num(max = 20000, step = 500) int RUN_FURTHEST_PORT = 1500;
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
        public @Option boolean IGNORE_CONTESTED_BOXES = true;

        @Option
        @Editor(value = JBoxInfoTable.class, shared = true)
        public Map<String, BoxInfo> BOX_INFOS = new HashMap<>();
        public transient Lazy<String> ADDED_BOX = new Lazy.NoCache<>();
    }

    public @Option Loot LOOT = new Loot();
    public static class Loot {
        public @Option Sab SAB = new Sab();
        public @Option Rsb RSB = new Rsb();
        public static class Sab {
            public @Option boolean ENABLED = false;
            public @Option Character KEY = '2';
            public @Option @Editor(JPercentField.class) double PERCENT = 0.8;
            public @Option @Num(min = 500, max = 1_000_000, step = 1000) int NPC_AMOUNT = 12000;
        }
        public static class Rsb {
            public @Option boolean ENABLED = false;
            public @Option Character KEY = '3';
            public @Option @Num(min = 500, max = 60_000, step = 500) int AMMO_REFRESH = 3500;
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
        public @Deprecated int MODULE = 0; // Kept so plugins using it don't just break. They'll just be unable to use pet.
        public @Option(value = "Legacy mode (old plugin compatibility)", description = "Enable if an older plugin is managing pet. The new pet selector becomes useless.") boolean COMPATIBILITY_MODE = false;
        public @Option @Editor(JListField.class) @Options(PetGearSupplier.class) int MODULE_ID = 1;
    }

    public @Option GroupSettings GROUP = new GroupSettings();
    public static class GroupSettings {
        public @Option boolean ACCEPT_INVITES = false;
        public @Option @Tag(TagDefault.ALL) PlayerTag WHITELIST_TAG = null;
        public @Option @Tag(TagDefault.NONE) PlayerTag INVITE_TAG = null;
        public @Option boolean OPEN_INVITES = false;
        //public @Option @Tag(TagDefault.NONE) PlayerTag KICK_TAG = null;
    }

    public @Option Miscellaneous MISCELLANEOUS = new Miscellaneous();
    public static class Miscellaneous {
        public @Option boolean REFRESH_AFTER_REVIVE = false;
        public @Option @Num(max = 60 * 12, step = 10) int REFRESH_TIME = 60;
        public @Option @Num(max = 60 * 12, step = 10) int PAUSE_FOR = 0;
        public @Option boolean RESET_REFRESH = true;
        public @Option @Editor(JPercentField.class) double DRONE_REPAIR_PERCENTAGE = 0.9;
        public @Option boolean HONOR_LOST_EXACT = true;
        public @Option boolean LOG_CHAT = false;
        public @Option boolean LOG_DEATHS = false;
    }

    public @Option BotSettings BOT_SETTINGS = new BotSettings();
    public static class BotSettings {
        public @Option BotGui BOT_GUI = new BotGui();
        public static class BotGui {
            @Option @Editor(LangEditor.class) @Options(LanguageSupplier.class)
            public Locale LOCALE = new Locale(Locale.getDefault().getLanguage());

            public @Option boolean CONFIRM_EXIT = true;
            public @Option boolean SAVE_GUI_POS = false;
            public @Option boolean CONFIG_TREE_TABS = true;
            public @Option @Num(min = 1, max = 20, step = 1) int BUTTON_SIZE = 4;

            public boolean ALWAYS_ON_TOP = true; // No @Option. Edited via button
            public WindowPosition MAIN_GUI_WINDOW = new WindowPosition();

            public static class WindowPosition {
                // x and y refer to top left coordinates of window
                public int x = Integer.MIN_VALUE, y = Integer.MIN_VALUE;
                public int width = MainGui.DEFAULT_WIDTH, height = MainGui.DEFAULT_HEIGHT;
            }
        }

        public @Option APIConfig API_CONFIG = new APIConfig();
        public static class APIConfig {
            public @Option @Editor(JListField.class) @Options(ApiSupplier.class) int API = 2;
            public @Option boolean FULLY_HIDE_API = true;
            public @Option boolean FORCE_GAME_LANGUAGE = false;
            public @Option boolean ENFORCE_HW_ACCEL = true;

            public int width = 1280;
            public int height = 800;
        }

        public @Option MapDisplay MAP_DISPLAY = new MapDisplay();
        public static class MapDisplay {
            @Option @Editor(JCheckboxListField.class) @Options(DisplayFlag.Supplier.class)
            public Set<DisplayFlag> TOGGLE = EnumSet.of(
                    HERO_NAME, HP_SHIELD_NUM, ZONES, STATS_AREA, BOOSTER_AREA, GROUP_NAMES, GROUP_AREA);
            public @Option @Num(max = 300, step = 1) int TRAIL_LENGTH = 15;
            public @Option boolean MAP_START_STOP = false;
            public @Option(key = "colors") ColorScheme cs = new ColorScheme();
        }

        public @Option Other OTHER = new Other();
        public static class Other {
            public @Option boolean DISABLE_MASTER_PASSWORD = false;
            public @Option @Num(min = 10, max = 300) int ZONE_RESOLUTION = 30;
            public @Option @Num(min = 10, max = 250) int MIN_TICK = 15;
            public @Option boolean DEV_STUFF = false;
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
            return "Config: " + CONFIG + "   Formation: " + JCharField.getDisplay(FORMATION);
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
