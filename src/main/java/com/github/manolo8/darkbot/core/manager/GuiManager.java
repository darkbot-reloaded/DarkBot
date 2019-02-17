package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.swf.Dictionary;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import static com.github.manolo8.darkbot.Main.API;

public class GuiManager implements Manager {

    private final Main main;
    private final Dictionary guis;

    private long reconnectTime;
    private long repairTime;
    private long validTime;

    private long repairAddress;

    private long screenAddress;
    private long guiAddress;
    private long mainAddress;

    private final Gui lostConnection;
    private final Gui connecting;
    public final PetManager pet;

    public boolean check;

    public int deaths;

    public GuiManager(Main main) {
        this.main = main;

        this.validTime = System.currentTimeMillis();
        this.guis = new Dictionary(0);

        this.lostConnection = new Gui(0);
        this.connecting = new Gui(0);
        this.pet = new PetManager(main);

        this.main.status.add(value -> validTime = System.currentTimeMillis());
    }

    @Override
    public void install(BotInstaller botInstaller) {

        this.guis.addLazy("lost_connection", lostConnection::update);
        this.guis.addLazy("connecting", value -> System.out.println("HAS CONNECTING!"));
        this.guis.addLazy("connection", connecting::update);
        this.guis.addLazy("pet", this.pet::update);

        botInstaller.screenManagerAddress.add(value -> screenAddress = value);
        botInstaller.mainAddress.add(value -> mainAddress = value);

        botInstaller.invalid.add(value -> {
            if (!value) validTime = System.currentTimeMillis();
        });

        botInstaller.guiManagerAddress.add(value -> {
            guiAddress = value;
            guis.update(API.readMemoryLong(guiAddress + 112));

            repairAddress = 0;
            lostConnection.reset();
            connecting.reset();
            pet.reset();

            check = true;
        });
    }

    public void tick() {
        guis.update();

        lostConnection.update();
        connecting.update();
        pet.update();
    }

    private void tryReconnect(Gui gui) {
        if (System.currentTimeMillis() - reconnectTime > 5000) {
            reconnectTime = System.currentTimeMillis();
            API.mouseClick(gui.x + 46, gui.y + 180);
        }
    }

    private void tryRevive() {
        if (System.currentTimeMillis() - repairTime > 10000) {
            deaths++;
            API.writeMemoryLong(repairAddress + 32, main.config.GENERAL.SAFETY.REVIVE_LOCATION);
            API.mouseClick(MapManager.clientWidth / 2, (MapManager.clientHeight / 2) + 190);
            repairTime = System.currentTimeMillis();
        }
    }

    private boolean isInvalidShip() {
        return API.readMemoryInt(API.readMemoryLong(screenAddress + 240) + 56) == 0;
    }

    private boolean isDead() {
        if (repairAddress != 0) {
            return API.readMemoryBoolean(repairAddress + 40);
        } else {
            if (isInvalidShip()) {

                long[] values = API.queryMemory(ByteUtils.getBytes(guiAddress, mainAddress), 1);

                if (values.length == 1)
                    repairAddress = values[0] - 56;

                return false;
            } else {
                return false;
            }
        }
    }

    private void checkInvalid() {
        if (System.currentTimeMillis() - validTime > 90 * 1000 + (main.hero.map.id == -1 ? 180 * 1000 : 0)) {
            API.refresh();
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
                API.refresh();
                connecting.reset();
            }

            return false;
        } else if (isDead()) {

            tryRevive();

            if (deaths >= main.config.GENERAL.SAFETY.MAX_DEATHS)
                main.setRunning(false);
            else
                checkInvalid();


            return false;
        } else if (System.currentTimeMillis() - repairTime < main.config.GENERAL.SAFETY.WAIT_AFTER_REVIVE * 1000) {
            validTime = System.currentTimeMillis();
            return false;
        } else if (main.hero.locationInfo.isMoving()) {
            validTime = System.currentTimeMillis();
        }

        checkInvalid();

        return true;
    }

}
