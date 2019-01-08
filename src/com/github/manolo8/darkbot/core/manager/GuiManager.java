package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.swf.Dictionary;

import static com.github.manolo8.darkbot.Main.API;

public class GuiManager implements Manager {

    private final Main main;
    private final Dictionary guis;

    private long reconnectTime;
    private long repairTime;
    private long repairTimePet;
    private long validTime;

    private long repairAddress;

    private long screenAddress;
    private long guiAddress;
    private long mainAddress;

    private final Gui pet;
    private final Gui lostConnection;
    private final Gui connecting;

    private boolean temp;
    private long timeTemp;
    private long petTemp;

    public boolean check;

    public int revives;
    public int petRevives;

    public GuiManager(Main main) {
        this.main = main;

        this.guis = new Dictionary(0);

        pet = new Gui(0);
        lostConnection = new Gui(0);
        connecting = new Gui(0);
    }

    @Override
    public void install(BotInstaller botInstaller) {

        this.guis.addLazy("lost_connection", lostConnection::update);
        this.guis.addLazy("connecting", connecting::update);
        this.guis.addLazy("pet", pet::update);

        botInstaller.screenManagerAddress.add(value -> screenAddress = value);
        botInstaller.mainAddress.add(value -> mainAddress = value);

        botInstaller.guiManagerAddress.add(value -> {
            guiAddress = value;
            guis.update(API.readMemoryLong(guiAddress + 112));

            repairAddress = 0;
            pet.reset();
            lostConnection.reset();
            connecting.reset();

            check = true;
        });
    }

    @Override
    public void stop() {
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
            revives++;
            API.writeMemoryLong(repairAddress + 32, 1);
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
                long[] values = API.queryMemoryLong(guiAddress, 1000);
                for (long result : values) {
                    result -= 56;

                    long address = API.readMemoryLong(result + 64);
                    int type = API.readMemoryInt(result + 32);
                    int dead = API.readMemoryInt(result + 40);

                    if (address == mainAddress && type >= 0 && type <= 3 && (dead == 0 || dead == 1)) {
                        repairAddress = result;
                        return dead == 1;
                    }
                }
                return false;
            } else {
                return false;
            }
        }
    }

    private void checkInvalid() {
        if (System.currentTimeMillis() - validTime > 90 * 1000 + (main.hero.map.id == -1 ? 180 * 1000 : 0)) {
            API.setDrawing(true);
            API.refresh();
            API.setDrawing(false);
            validTime = System.currentTimeMillis();
        }
    }

    public boolean canTickModule() {

        if (lostConnection.visible) {

            tryReconnect(lostConnection);

            checkInvalid();

            return false;
        } else if (connecting.visible) {

            checkInvalid();

            return false;
        } else if (isDead()) {
            tryRevive();

            if (revives > main.config.MAX_DEATHS) {
                main.setRunning(false);
            } else {
                checkInvalid();
            }

            return false;
        } else if (isInvalidShip()) {
            checkInvalid();
        } else {
            validTime = System.currentTimeMillis();
        }

        return true;
    }

}
