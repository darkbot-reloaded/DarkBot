package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.OreTradeGui;
import com.github.manolo8.darkbot.core.objects.RefinementGui;
import com.github.manolo8.darkbot.core.objects.TargetedOfferGui;
import com.github.manolo8.darkbot.core.objects.facades.SettingsProxy;
import com.github.manolo8.darkbot.core.objects.facades.SlotBarsProxy;
import com.github.manolo8.darkbot.core.objects.facades.StatsProxy;
import com.github.manolo8.darkbot.core.objects.swf.PairArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.entities.utils.Area;
import eu.darkbot.api.managers.GameScreenAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static com.github.manolo8.darkbot.Main.API;

public class GuiManager implements Manager, GameScreenAPI {

    private final Main main;
    private final PluginAPI pluginAPI;
    private final SlotBarsProxy slotBarsProxy;
    private final SettingsProxy settingsProxy;
    private final StatsProxy statsProxy;

    private final PairArray guis = PairArray.ofDictionary();

    private long reconnectTime;
    private long lastDeath = -1;
    private long lastRepair;
    private long validTime;

    private long repairAddress;

    private long screenAddress;
    private long guiAddress;
    private long mainAddress;

    private final List<Gui> registeredGuis = new ArrayList<>();

    public final Gui lostConnection;
    public final Gui connecting;
    public final Gui quests;
    public final Gui minimap;
    public final Gui targetedOffers;
    public final Gui logout;
    public final Gui eventProgress;
    public final Gui eternalGate;
    public final Gui blacklightGate;
    public final RefinementGui refinement;
    public final PetManager pet;
    public final OreTradeGui oreTrade;
    public final GroupManager group;

    private LoadStatus checks = LoadStatus.WAITING;
    private enum LoadStatus {
        WAITING(gm -> gm.quests.lastUpdatedIn(5000) && gm.quests.visible),
        MISSION_CLOSING(gm -> gm.quests.show(false)),
        CLICKING_AMMO(gm -> {
            API.keyboardClick(gm.main.config.LOOT.AMMO_KEY);
            return true;
        }),
        DONE(q -> false);

        Predicate<GuiManager> canAdvance;
        LoadStatus(Predicate<GuiManager> next) {
            this.canAdvance = next;
        }
    }

    public int deaths;

    private boolean needRefresh;

    public GuiManager(Main main, PluginAPI pluginAPI) {
        this.main = main;
        this.pluginAPI = pluginAPI;
        this.slotBarsProxy = pluginAPI.requireInstance(SlotBarsProxy.class);
        this.settingsProxy = pluginAPI.requireInstance(SettingsProxy.class);
        this.statsProxy = pluginAPI.requireInstance(StatsProxy.class);

        this.validTime = System.currentTimeMillis();

        this.pet = register("pet", PetManager.class);
        this.oreTrade = register("ore_trade", OreTradeGui.class);
        this.group = register("group", GroupManager.class);

        this.main.status.add(value -> validTime = System.currentTimeMillis());
        this.lostConnection = register("lost_connection");
        this.connecting = register("connection");
        this.quests = register("quests");
        this.minimap = register("minimap");
        this.targetedOffers = register("targetedOffers", TargetedOfferGui.class);
        this.logout = register("logout");
        this.eventProgress = register("eventProgress");
        this.eternalGate = register("eternal_gate");
        this.blacklightGate = register("eternal_blacklight");
        this.refinement = register("refinement", RefinementGui.class);
    }

    public Gui register(String key) {
        return register(key, Gui.class);
    }

    @SuppressWarnings({"unchecked", "CastCanBeRemovedNarrowingVariableType"})
    public <T extends Gui> T register(String key, Class<T> gui) {
        Gui guiFix = pluginAPI.requireInstance(gui); // Workaround for a java compiler assertion bug having issues with types
        this.guis.addLazy(key, guiFix::update);
        this.registeredGuis.add(guiFix);

        return (T) guiFix;
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.screenManagerAddress.add(value -> screenAddress = value);
        botInstaller.mainAddress.add(value -> mainAddress = value);

        botInstaller.invalid.add(value -> {
            if (!value) {
                validTime = System.currentTimeMillis();
                checks = LoadStatus.WAITING;
            }
            API.resetCache();
        });

        botInstaller.guiManagerAddress.add(value -> {
            guiAddress = value;
            guis.update(API.readMemoryLong(guiAddress + 112));

            repairAddress = 0;
            registeredGuis.forEach(Gui::reset);
            checks = LoadStatus.WAITING;
        });
    }

    public void tick() {
        guis.update();

        registeredGuis.forEach(Gui::update);

        if (checks != LoadStatus.DONE && checks.canAdvance.test(this))
            checks = LoadStatus.values()[checks.ordinal() + 1];
        targetedOffers.show(false);
    }

    private void tryReconnect(Gui gui) {
        if (System.currentTimeMillis() - reconnectTime > 5000) {
            reconnectTime = System.currentTimeMillis();
            gui.click(46, 180);
        }
    }

    public boolean tryRevive() {
        if (System.currentTimeMillis() - lastRepair > 10000) {
            deaths++;
            long respawnId = main.config.GENERAL.SAFETY.REVIVE_LOCATION;
            if (main.repairManager.canRespawn((int) respawnId)) {
                API.writeMemoryLong(repairAddress + 32, respawnId);
            }
            API.mouseClick(MapManager.clientWidth / 2, (MapManager.clientHeight / 2) + 190);
            lastRepair = System.currentTimeMillis();
            if (main.config.MISCELLANEOUS.DRONE_REPAIR_PERCENTAGE != 0) this.main.backpage.checkDronesAfterKill();
            return true;
        }
        return false;
    }

    private boolean isInvalidShip() {
        return API.readMemoryInt(API.readMemoryLong(screenAddress + 240) + 56) == 0;
    }

    private boolean isDead() {
        if (repairAddress != 0) {
            return API.readMemoryBoolean(repairAddress + 40);
        } else if (isInvalidShip()) {
            long[] values = API.queryMemory(ByteUtils.getBytes(guiAddress, mainAddress), 1);
            if (values.length == 1) repairAddress = values[0] - 56;
        }
        return false;
    }

    private void checkInvalid() {
        if (System.currentTimeMillis() - validTime > 90_000 + (main.hero.map.id == -1 ? 180_000 : 0)) {
            System.out.println("Triggering refresh: gui manger was invalid for too long. " +
                    "(Make sure your hp fills up, equip an auto-repair CPU if you're missing one)");
            API.handleRefresh();
            validTime = System.currentTimeMillis();
        }
    }

    public boolean canTickModule() {

        if (lostConnection.visible) {
            //Wait 15 seconds to reconnect
            if (lostConnection.lastUpdatedIn(2500)) {
                tryReconnect(lostConnection);
                checkInvalid();
            }
            return false;
        } else if (connecting.visible) {

            if (connecting.lastUpdatedIn(30000)) {
                System.out.println("Triggering refresh: connection window stuck for too long");
                API.handleRefresh();
                connecting.reset();
            }

            return false;
        }

        if (isDead()) {
            this.needRefresh = true;
            main.hero.drive.stop(false);

            if (lastDeath == -1) lastDeath = System.currentTimeMillis();

            if (System.currentTimeMillis() - lastDeath < (main.config.GENERAL.SAFETY.WAIT_BEFORE_REVIVE * 1000)
                    || !tryRevive()) return false;

            lastDeath = -1;

            if (deaths >= main.config.GENERAL.SAFETY.MAX_DEATHS) main.setRunning(false);
            else checkInvalid();

            return false;
        } else {
            lastDeath = -1;
        }



        HeroManager hero = main.hero;
        if (this.needRefresh && System.currentTimeMillis() - lastRepair > 5_000) {
            this.needRefresh = false;
            if (main.config.MISCELLANEOUS.REFRESH_AFTER_REVIVE) {
                System.out.println("Triggering refresh: refreshing after death");
                API.handleRefresh();
                return false;
            }
        }
        if (System.currentTimeMillis() - lastRepair < main.config.GENERAL.SAFETY.WAIT_AFTER_REVIVE * 1000) {
            validTime = System.currentTimeMillis();
            return false;
        } else if (hero.locationInfo.isLoaded()
                && (hero.locationInfo.isMoving() || System.currentTimeMillis() - hero.drive.lastMoved > 20 * 1000)
                && (hero.health.hpIncreasedIn(30_000) || hero.health.hpDecreasedIn(30_000) || hero.health.hpPercent() == 1 || (hero.hasTarget() && hero.isAttacking(hero.target)))
                && (hero.health.shIncreasedIn(30_000) || hero.health.shDecreasedIn(30_000) || hero.health.shieldPercent() == 1 || hero.health.shieldPercent() == 0)) {
            validTime = System.currentTimeMillis();
        }

        checkInvalid();

        return main.hero.locationInfo.isLoaded();
    }

    @Override
    public Area.Rectangle getViewBounds() {
        return main.mapManager.screenBound;
    }

    @Override
    public Collection<? extends eu.darkbot.api.objects.Gui> getGuis() {
        return registeredGuis;
    }

    @Override
    public int getFps() {
        return statsProxy.getFps();
    }

    @Override
    public int getMemory() {
        return statsProxy.getMemory();
    }

    @Override
    public void zoomIn() {
        settingsProxy.getCharacterOf(SettingsProxy.KeyBind.ZOOM_IN)
                .ifPresent(API::keyboardClick);
    }

    @Override
    public void zoomOut() {
        settingsProxy.getCharacterOf(SettingsProxy.KeyBind.ZOOM_OUT)
                .ifPresent(API::keyboardClick);
    }

    //??
    @Override
    public void focusOnChat() {
        settingsProxy.getCharacterOf(SettingsProxy.KeyBind.FOCUS_CHAT)
                .ifPresent(API::keyboardClick);
    }

    @Override
    public void toggleMonitoring() {
        settingsProxy.getCharacterOf(SettingsProxy.KeyBind.TOGGLE_MONITORING)
                .ifPresent(API::keyboardClick);
    }

    @Override
    public void toggleWindows() {
        settingsProxy.getCharacterOf(SettingsProxy.KeyBind.TOGGLE_WINDOWS)
                .ifPresent(API::keyboardClick);
    }

    @Override
    public void toggleCategoryBar(boolean visible) {
        settingsProxy.getCharacterOf(SettingsProxy.KeyBind.TOGGLE_CATEGORYBAR)
                .filter(c -> slotBarsProxy.isCategoryBarVisible() != visible)
                .ifPresent(API::keyboardClick);
    }

    @Override
    public void toggleProActionBar(boolean visible) {
        settingsProxy.getCharacterOf(SettingsProxy.KeyBind.TOGGLE_PRO_ACTION)
                .filter(c -> slotBarsProxy.proActionBar.address != 0 && slotBarsProxy.isProActionBarVisible() != visible)
                .ifPresent(API::keyboardClick);
    }
}
