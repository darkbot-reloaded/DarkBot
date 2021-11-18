package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.types.suppliers.BrowserApi;
import com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag;
import com.github.manolo8.darkbot.config.types.suppliers.LanguageSupplier;
import com.github.manolo8.darkbot.config.types.suppliers.ModuleSupplier;
import com.github.manolo8.darkbot.config.types.suppliers.PetGears;
import com.github.manolo8.darkbot.config.types.suppliers.ReviveLocation;
import com.github.manolo8.darkbot.config.utils.ItemUtils;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.tree.editors.CharacterEditor;
import com.github.manolo8.darkbot.gui.tree.utils.NpcTableModel;
import com.github.manolo8.darkbot.gui.tree.utils.TableHelpers;
import eu.darkbot.api.config.annotations.Dropdown;
import eu.darkbot.api.config.annotations.Number;
import eu.darkbot.api.config.annotations.Option;
import eu.darkbot.api.config.annotations.Percentage;
import eu.darkbot.api.config.annotations.Table;
import eu.darkbot.api.config.annotations.Tag;
import eu.darkbot.api.config.types.ShipMode;
import eu.darkbot.api.game.enums.PetGear;
import eu.darkbot.api.game.items.ItemCategory;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.shared.modules.LootCollectorModule;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag.BOOSTER_AREA;
import static com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag.GROUP_AREA;
import static com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag.GROUP_NAMES;
import static com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag.HERO_NAME;
import static com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag.HP_SHIELD_NUM;
import static com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag.STATS_AREA;
import static com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag.ZONES;
import static com.github.manolo8.darkbot.config.types.suppliers.DisplayFlag.SHOW_PET;

public class Config implements eu.darkbot.api.config.legacy.Config {

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
    public static class General implements eu.darkbot.api.config.legacy.General {
        @Option @Dropdown(options = ModuleSupplier.class)
        public String CURRENT_MODULE = LootCollectorModule.class.getCanonicalName();
        public @Option @Dropdown(options = StarManager.MapOptions.class) int WORKING_MAP = 26;
        public @Option ShipConfig OFFENSIVE = new ShipConfig(1, '8');
        public @Option ShipConfig ROAM = new ShipConfig(1, '9');
        public @Option ShipConfig RUN = new ShipConfig(2, '9');
        public @Option @Number(max = 3600) int FORMATION_CHECK = 180;

        public @Option Safety SAFETY = new Safety();
        public static class Safety implements eu.darkbot.api.config.legacy.General.Safety {
            public @Option PercentRange REPAIR_HP_RANGE = new PercentRange(0.4, 0.95);
            public @Option @Percentage double REPAIR_HP_NO_NPC = 0.5;
            public @Option @Percentage double REPAIR_TO_SHIELD = 1;
            public @Option ShipConfig REPAIR = new ShipConfig(1, '9');
            public @Option @Number(min = 1, max = 9999) int MAX_DEATHS = 10;
            public @Option @Dropdown ReviveLocation REVIVE_LOCATION = ReviveLocation.BASE;
            public @Option @Number(min = 5, max = 60, step = 10) int WAIT_BEFORE_REVIVE = 5;
            public @Option @Number(min = 3, max = 15 * 60, step = 10) int WAIT_AFTER_REVIVE = 90;

            @Override
            public eu.darkbot.api.config.types.PercentRange getRepairHealthRange() {
                return REPAIR_HP_RANGE;
            }

            @Override
            public double getRepairHealthNoNpc() {
                return REPAIR_HP_NO_NPC;
            }

            @Override
            public double getRepairToShield() {
                return REPAIR_TO_SHIELD;
            }

            @Override
            public ShipMode getRepairMode() {
                return REPAIR;
            }
        }

        public @Option Running RUNNING = new Running();
        public static class Running implements eu.darkbot.api.config.legacy.General.Running {
            public @Option boolean RUN_FROM_ENEMIES = true;
            public @Option @Number(max = 24 * 60 * 60, step = 300) int REMEMBER_ENEMIES_FOR = 300;
            public @Option boolean RUN_FROM_ENEMIES_SIGHT = false;
            public @Option boolean STOP_RUNNING_NO_SIGHT = true;
            public @Option @Number(min = 500, max = 20000, step = 500) int MAX_SIGHT_DISTANCE = 4000;
            public @Option Character SHIP_ABILITY;
            public @Option @Number(max = 20000, step = 500) int SHIP_ABILITY_MIN = 1500;
            public @Option @Number(max = 20000, step = 500) int RUN_FURTHEST_PORT = 1500;

            @Override
            public boolean getRunFromEnemies() {
                return RUN_FROM_ENEMIES;
            }

            @Override
            public Duration getEnemyRemember() {
                return Duration.ofSeconds(REMEMBER_ENEMIES_FOR);
            }

            @Override
            public boolean getRunInSight() {
                return RUN_FROM_ENEMIES_SIGHT;
            }

            @Override
            public boolean getStopRunning() {
                return STOP_RUNNING_NO_SIGHT;
            }

            @Override
            public int getMaxSightDistance() {
                return MAX_SIGHT_DISTANCE;
            }

            @Override
            public int getRunClosestDistance() {
                return RUN_FURTHEST_PORT;
            }

            @Override
            public int getShipAbilityMinDistance() {
                return SHIP_ABILITY_MIN;
            }

            @Override
            public SelectableItem getShipAbility() {
                return ItemUtils.findAssociatedItem(null, SHIP_ABILITY).orElse(null);
            }
        }

        public @Option Roaming ROAMING = new Roaming();
        public static class Roaming {
            public @Option boolean KEEP = true;
            public @Option boolean SEQUENTIAL = false;
            public @Option boolean ONLY_KILL_PREFERRED = false;
            public @Option boolean ENEMY_CBS_INVISIBLE = false;
        }

        @Override
        public GameMap getWorkingMap() {
            return StarManager.getInstance().byId(WORKING_MAP);
        }

        @Override
        public eu.darkbot.api.config.legacy.General.Safety getSafety() {
            return SAFETY;
        }

        @Override
        public eu.darkbot.api.config.legacy.General.Running getRunning() {
            return RUNNING;
        }
    }

    public @Option Collect COLLECT = new Collect();
    public static class Collect implements eu.darkbot.api.config.legacy.Collect {
        public @Option boolean STAY_AWAY_FROM_ENEMIES;
        public @Option boolean AUTO_CLOACK;
        public @Option Character AUTO_CLOACK_KEY;
        public @Option @Number(max = 10000, step = 50) int RADIUS = 400;
        public @Option boolean IGNORE_CONTESTED_BOXES = true;

        @Option
        @Table(decorator = TableHelpers.BoxInfoDecorator.class)
        public Map<String, BoxInfo> BOX_INFOS = new HashMap<>();
        public transient Lazy<String> ADDED_BOX = new Lazy.NoCache<>();

        @Override
        public boolean getAutoCloak() {
            return AUTO_CLOACK;
        }

        @Override
        public boolean getStayAwayFromEnemies() {
            return STAY_AWAY_FROM_ENEMIES;
        }

        @Override
        public boolean getIgnoreContestedBoxes() {
            return IGNORE_CONTESTED_BOXES;
        }
    }

    public @Option Loot LOOT = new Loot();
    public static class Loot {
        public @Option Sab SAB = new Sab();
        public @Option Rsb RSB = new Rsb();
        public static class Sab {
            public @Option boolean ENABLED = false;
            public @Option Character KEY = '2';
            public @Option @Percentage double PERCENT = 0.8;
            public @Option @Number(min = 500, max = 1_000_000, step = 1000) int NPC_AMOUNT = 12000;
            public @Option() Condition CONDITION;
        }
        public static class Rsb {
            public @Option boolean ENABLED = false;
            public @Option Character KEY = '3';
            public @Deprecated int AMMO_REFRESH = 3500;
        }
        public @Option Character AMMO_KEY = '1';
        public @Option Character SHIP_ABILITY;
        public @Option @Number(min = 50_000, max = 5_000_000, step = 50_000) int SHIP_ABILITY_MIN = 150_000;
        public @Option @Number(max = 10, step = 1) int MAX_CIRCLE_ITERATIONS = 5;
        public @Option boolean RUN_CONFIG_IN_CIRCLE = true;

        public @Option boolean GROUP_NPCS = true;
        @Table(controls = {Table.Control.SEARCH, Table.Control.CUSTOM, Table.Control.ADD, Table.Control.REMOVE},
                customControls = TableHelpers.MapPickerBuilder.class,
                customModel = NpcTableModel.class,
                decorator = TableHelpers.NpcTableDecorator.class)
        public @Option Map<String, NpcInfo> NPC_INFOS = new HashMap<>();
        public transient Lazy<String> MODIFIED_NPC = new Lazy.NoCache<>();

        public @Option @Number(min = 1000, max = 20000, step = 500) int NPC_DISTANCE_IGNORE = 3000;
    }

    public @Option PetSettings PET = new PetSettings();
    public static class PetSettings {
        public @Option boolean ENABLED = false;
        public @Option @Dropdown(options = PetGears.class) PetGear MODULE_ID = PetGear.PASSIVE;
    }

    public @Option GroupSettings GROUP = new GroupSettings();
    public static class GroupSettings {
        public @Option boolean ACCEPT_INVITES = false;
        public @Option @Tag(Tag.Default.ALL) PlayerTag WHITELIST_TAG = null;
        public @Option @Tag(Tag.Default.NONE) PlayerTag INVITE_TAG = null;
        public @Option boolean OPEN_INVITES = false;
        public @Option boolean LEAVE_NO_WHITELISTED = false;
        //public @Option @Tag(TagDefault.NONE) PlayerTag KICK_TAG = null;
    }

    public @Option Miscellaneous MISCELLANEOUS = new Miscellaneous();
    public static class Miscellaneous {
        public @Option boolean REFRESH_AFTER_REVIVE = false;
        public @Option @Number(max = 60 * 12, step = 10) int REFRESH_TIME = 60;
        public @Option @Number(max = 60 * 12, step = 10) int PAUSE_FOR = 0;
        public @Option boolean RESET_REFRESH = true;
        public @Option @Percentage double DRONE_REPAIR_PERCENTAGE = 0.9;
        public @Option boolean HONOR_LOST_EXACT = true;
        public @Option boolean LOG_CHAT = false;
        public @Option boolean LOG_DEATHS = false;
    }

    public @Option BotSettings BOT_SETTINGS = new BotSettings();
    public static class BotSettings {
        public @Option BotGui BOT_GUI = new BotGui();
        public static class BotGui {
            @Option @Dropdown(options = LanguageSupplier.class)
            public Locale LOCALE = new Locale(Locale.getDefault().getLanguage());

            public @Option boolean CONFIRM_EXIT = true;
            public @Option boolean SAVE_GUI_POS = false;
            public @Option @Number(min = 1, max = 20, step = 1) int BUTTON_SIZE = 4;

            public boolean ALWAYS_ON_TOP = true; // No @Option. Edited via button
            public WindowPosition MAIN_GUI_WINDOW = new WindowPosition();
            public WindowPosition CONFIG_GUI_WINDOW = new WindowPosition();

            public static class WindowPosition {
                // x and y refer to top left coordinates of window
                public int x = Integer.MIN_VALUE, y = Integer.MIN_VALUE;
                public int width = MainGui.DEFAULT_WIDTH, height = MainGui.DEFAULT_HEIGHT;
            }
        }

        public @Option APIConfig API_CONFIG = new APIConfig();
        public static class APIConfig {
            public @Option @Dropdown BrowserApi BROWSER_API = BrowserApi.DARK_BOAT;
            public @Option boolean FULLY_HIDE_API = true;
            public @Option boolean FORCE_GAME_LANGUAGE = false;
            public @Option boolean ENFORCE_HW_ACCEL = true;

            public int width = 1280;
            public int height = 800;
        }

        public @Option MapDisplay MAP_DISPLAY = new MapDisplay();
        public static class MapDisplay {
            @Option @Dropdown(multi = true)
            public Set<DisplayFlag> TOGGLE = EnumSet.of(
                    HERO_NAME, HP_SHIELD_NUM, ZONES, STATS_AREA, BOOSTER_AREA, GROUP_NAMES, GROUP_AREA, SHOW_PET);
            public @Option @Number(max = 300, step = 1) int TRAIL_LENGTH = 15;
            public @Option boolean MAP_START_STOP = false;
            public @Option("colors") ColorScheme cs = new ColorScheme();
        }

        public @Option Other OTHER = new Other();
        public static class Other {
            public @Option boolean DISABLE_MASTER_PASSWORD = false;
            public @Option @Number(min = 10, max = 300) int ZONE_RESOLUTION = 30;
            public @Option @Number(min = 10, max = 250) int MIN_TICK = 15;
            public @Option boolean DEV_STUFF = false;
        }
    }

    public /*@Option("Extra actions")*/ ExtraActions EXTRA = new ExtraActions();
    public static class ExtraActions {
        // Dummy testing condition
        public @Option Condition CONDITION;

        public @Option @Table Map<String, ActionInfo> ACTION_INFOS = new HashMap<>();
        public transient Lazy<String> MODIFIED_ACTIONS = new Lazy.NoCache<>();
    }

    public static class ShipConfig implements ShipMode {
        public int CONFIG = 1;
        public Character FORMATION;

        public ShipConfig() {}
        public ShipConfig(int CONFIG, Character FORMATION) {
            this.CONFIG = CONFIG;
            this.FORMATION = FORMATION;
        }

        @Override
        public HeroAPI.Configuration getConfiguration() {
            return HeroAPI.Configuration.of(CONFIG);
        }

        @Override
        public SelectableItem.Formation getFormation() {
            return ItemUtils.findAssociatedItem(ItemCategory.DRONE_FORMATIONS, FORMATION)
                    .map(it -> SelectableItem.Formation.of(it.id))
                    .orElse(SelectableItem.Formation.STANDARD);
        }

        @Override
        public String toString() {
            return "Config: " + CONFIG + "   Formation: " + CharacterEditor.getDisplay(FORMATION);
        }
    }

    public static class PercentRange implements eu.darkbot.api.config.types.PercentRange {
        public double min, max;

        public PercentRange() {}
        public PercentRange(double min, double max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public double getMin() {
            return min;
        }

        @Override
        public double getMax() {
            return max;
        }

        @Override
        public String toString() {
            return Math.round(min * 100) + "%-" + Math.round(max * 100) + "%";
        }
    }


    @Override
    public Collection<? extends eu.darkbot.api.config.types.SafetyInfo> getSafeties(GameMap gameMap) {
        return SAFETY.getOrDefault(gameMap.getId(), Collections.emptySet());
    }

    @Override
    public Map<Integer, ? extends eu.darkbot.api.config.types.PlayerInfo> getPlayerInfos() {
        return PLAYER_INFOS;
    }

    @Override
    public eu.darkbot.api.config.legacy.General getGeneral() {
        return GENERAL;
    }

    @Override
    public eu.darkbot.api.config.legacy.Collect getCollect() {
        return COLLECT;
    }
}
