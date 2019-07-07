package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.swf.Dictionary;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import java.util.function.Predicate;

import static com.github.manolo8.darkbot.Main.API;

public class GuiManager implements Manager {

    private final Main main;
    private final Dictionary guis;

    private long reconnectTime;
    private long lastDeath = -1;
    private long lastRepair;
    private long validTime;

    private long repairAddress;

    private long screenAddress;
    private long guiAddress;
    private long mainAddress;

    private final Gui lostConnection;
    private final Gui connecting;
    private final Gui quests;
    public final Gui eventProgress;
    public final Gui oreTrade;
    public final PetManager pet;

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
        this.guis = new Dictionary(0);

        this.lostConnection = new Gui();
        this.connecting = new Gui();
        this.quests = new Gui();
        this.eventProgress = new Gui();
        this.oreTrade = new Gui();
        this.pet = new PetManager(main);

        this.main.status.add(value -> validTime = System.currentTimeMillis());
    }

    @Override
    public void install(BotInstaller botInstaller) {

        this.guis.addLazy("lost_connection", lostConnection::update);
        this.guis.addLazy("connection", connecting::update);
        this.guis.addLazy("quests", this.quests::update);
        this.guis.addLazy("eventProgress", this.eventProgress::update);
        this.guis.addLazy("ore_trade", this.oreTrade::update);
        this.guis.addLazy("pet", this.pet::update);

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
            lostConnection.reset();
            connecting.reset();
            eventProgress.reset();
            oreTrade.reset();
            pet.reset();
            checks = LoadStatus.WAITING;
        });
    }

    public void tick() {
        guis.update();

        lostConnection.update();
        connecting.update();
        quests.update();
        eventProgress.update();
        oreTrade.update();
        pet.update();


        if (checks != LoadStatus.DONE && checks.canAdvance.test(quests)) {
            if (checks == LoadStatus.CLICKING_AMMO) API.keyboardClick(main.config.LOOT.AMMO_KEY);
            checks = LoadStatus.values()[checks.ordinal() + 1];
        }
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
            if (main.config.MISCELLANEOUS.REPAIR_DRONE_PERCENTAGE != 0) this.main.backpage.checkDronesAfterKill();
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


        if (System.currentTimeMillis() - lastRepair < main.config.GENERAL.SAFETY.WAIT_AFTER_REVIVE * 1000) {
            validTime = System.currentTimeMillis();
            return false;
        } else if (main.hero.locationInfo.isLoaded()
                && (main.hero.locationInfo.isMoving() || System.currentTimeMillis() - main.hero.drive.lastMoved > 20 * 1000)
                && (main.hero.health.hpIncreasedIn(30_000) || main.hero.health.hpDecreasedIn(30_000) || main.hero.health.hpPercent() == 1)
                && (main.hero.health.shIncreasedIn(30_000) || main.hero.health.shDecreasedIn(30_000) || main.hero.health.shieldPercent() == 1 || main.hero.health.shieldPercent() == 0)) {
            validTime = System.currentTimeMillis() - main.pingManager.ping;
        }

        checkInvalid();

        return main.hero.locationInfo.isLoaded();
    }

}
