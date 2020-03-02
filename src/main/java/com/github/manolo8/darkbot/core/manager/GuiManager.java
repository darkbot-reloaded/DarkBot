package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.TargetedOfferGui;
import com.github.manolo8.darkbot.core.objects.OreTradeGui;
import com.github.manolo8.darkbot.core.objects.swf.Dictionary;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.github.manolo8.darkbot.Main.API;

public class GuiManager implements Manager {

    private final Main main;
    private final Dictionary guis = new Dictionary();

    private long reconnectTime;
    private long lastDeath = -1;
    private long lastRepair;
    private long validTime;

    private long repairAddress;

    private long screenAddress;
    private long guiAddress;
    private long mainAddress;

    private List<Gui> registeredGuis = new ArrayList<>();

    public final Gui lostConnection = register("lost_connection");
    public final Gui connecting = register("connection");
    public final Gui quests = register("quests");
    public final Gui targetedOffers = register("targetedOffers", new TargetedOfferGui());
    public final Gui logout = register("logout");
    public final Gui eventProgress =  register("eventProgress");
    public final PetManager pet;
    public final OreTradeGui oreTrade;
    public final GroupManager group;

    private LoadStatus checks = LoadStatus.WAITING;
    private enum LoadStatus {
        WAITING(q -> q.lastUpdatedIn(5000) && q.visible),
        MISSION_CLOSING(q -> q.show(false)),
        CLICKING_AMMO(q -> true), DONE(q -> false);
        Predicate<Gui> canAdvance;
        LoadStatus(Predicate<Gui> next) {
            this.canAdvance = next;
        }
    }

    public int deaths;

    public GuiManager(Main main) {
        this.main = main;

        this.validTime = System.currentTimeMillis();

        this.pet = register("pet", new PetManager(main));
        this.oreTrade = register("ore_trade", new OreTradeGui(main));
        this.group = register("group", new GroupManager(main));

        this.main.status.add(value -> validTime = System.currentTimeMillis());
    }

    public Gui register(String key) {
        return register(key, new Gui());
    }

    public <T extends Gui> T register(String key, T gui) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        Gui guiFix = gui; // Workaround for a java compiler assertion bug having issues with types
        this.guis.addLazy(key, guiFix::update);
        this.registeredGuis.add(guiFix);
        return gui;
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

        if (checks != LoadStatus.DONE && checks.canAdvance.test(quests)) {
            if (checks == LoadStatus.CLICKING_AMMO) API.keyboardClick(main.config.LOOT.AMMO_KEY);
            checks = LoadStatus.values()[checks.ordinal() + 1];
        }
        targetedOffers.show(false);
    }

    private void tryReconnect(Gui gui) {
        if (System.currentTimeMillis() - reconnectTime > 5000) {
            reconnectTime = System.currentTimeMillis();
            gui.click(46, 180);
        }
    }

    private boolean tryRevive() {
        if (System.currentTimeMillis() - lastRepair > 10000) {
            deaths++;
            API.writeMemoryLong(repairAddress + 32, main.config.GENERAL.SAFETY.REVIVE_LOCATION);
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
            System.out.println("Triggering refresh: gui manger was invalid for too long");
            API.handleRefresh();
            validTime = System.currentTimeMillis();
        }
    }

    public boolean canTickModule() {

        if (lostConnection.visible) {
            //Wait 15 seconds to reconnect
            if (lostConnection.lastUpdatedIn(25000)) {
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
        if (System.currentTimeMillis() - lastRepair < main.config.GENERAL.SAFETY.WAIT_AFTER_REVIVE * 1000) {
            validTime = System.currentTimeMillis();
            return false;
        } else if (hero.locationInfo.isLoaded()
                && (hero.locationInfo.isMoving() || System.currentTimeMillis() - hero.drive.lastMoved > 20 * 1000)
                && (hero.health.hpIncreasedIn(30_000) || hero.health.hpDecreasedIn(30_000) || hero.health.hpPercent() == 1 || (hero.hasTarget() && hero.isAttacking(hero.target)))
                && (hero.health.shIncreasedIn(30_000) || hero.health.shDecreasedIn(30_000) || hero.health.shieldPercent() == 1 || hero.health.shieldPercent() == 0)) {
            validTime = main.pingManager.lastPingUpdate() + 60_000;
        }

        checkInvalid();

        return main.hero.locationInfo.isLoaded();
    }

}
