package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.def.Manager;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.Vector;
import com.github.manolo8.darkbot.core.utils.pet.PetModule;

import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class GuiManager implements Manager {

    private final Main main;
    private final Vector guiReader;

    private long reconnectTime;
    private long repairTime;
    private long repairTimePet;
    private long validTime;

    private long repairAddress;

    private long screenAddress;
    private long guiAddress;
    private long mainAddress;

    private Gui petGui;
    private Gui lostConnection;
    private Gui connecting;

    private boolean temp;
    private long timeTemp;

    private boolean check;

    public int revives;
    public int maxRevives = 30;
    public int petRevives;
    public int maxPetRevives = 1000;

    public boolean nullPetModuleOnActivate;
    public PetModule module;

    public GuiManager(Main main) {
        this.main = main;
        this.guiReader = new Vector(0);
    }

    @Override
    public void install(BotManager botManager) {

        screenAddress = botManager.screenManagerAddress;
        guiAddress = botManager.guiManagerAddress;
        mainAddress = botManager.mainAddress;

        guiReader.update(API.readMemoryLong(guiAddress + 112));

        //reset

        check = true;
        petGui = null;
    }

    @Override
    public void stop() {
        repairAddress = 0;
    }

    public void tick() {

        if (lostConnection == null || connecting == null || petGui == null) {
            guiReader.update();
        }

        if (lostConnection == null) {
            lostConnection = tempGui("lost_connection");
        } else {
            lostConnection.update();
        }

        if (connecting == null) {
            connecting = tempGui("connecting");
        } else {
            connecting.update();
        }

//        if (petGui == null) {
//            petGui = tempGui("pet");
//        } else {
//            petGui.update();
//
//            if (main.hero.pet.isInvalid()) {
//
//                if (petRevives < maxPetRevives) {
//                    tryActivatePet();
//                }
//
//                temp = true;
//                timeTemp = System.currentTimeMillis();
//
//                check = true;
//
//            } else if (check && module != null) {
//
//                module.update(petGui);
//
//                if (!module.isEnabled() && module.enable()) {
//
//                    check = false;
//
//                    if (nullPetModuleOnActivate) module = null;
//
//                    petGui.show(false);
//                }
//            } else if (temp && System.currentTimeMillis() - timeTemp > 4000) {
//                API.button('R');
//                temp = false;
//            }
//
//        }
    }

    private Gui tempGui(String key) {
        Vector.Entry entry = guiReader.get(key);
        return entry != null ? new Gui(API.readMemoryLong(entry.value + 488), entry.key) : null;
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

    boolean b;

    private void tryActivatePet() {
        if (System.currentTimeMillis() - repairTimePet > 1000) {

            if (petGui != null && petGui.show(true)) {


                Gui.PixelHelper helper = petGui.createHelper(0, 100);

                for (int i = 0; i < helper.size; i++) {

                    int c = helper.pixels[i];

                    if (c == 16247715
                            && helper.add(i, -9, 1) == 16504241
                            && helper.add(i, 1, -7) == 16304523
                            && helper.add(i, -5, -6) == 5592405
                            && helper.add(i, 7, 7) == 16240271
                    ) {
                        petRevives++;
                        helper.click(i);
                    } else if (c == 16370323
                            && helper.add(i, 0, 11) == 16370066
                            && helper.add(i, 12, 6) == 16434070
                    ) {
                        helper.click(i);
                    }

                }

            }

            repairTimePet = System.currentTimeMillis();
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
                List<Long> values = API.queryMemory(guiAddress);
                for (long result : values) {
                    if (API.readMemoryLong(result + 8) == mainAddress) {
                        repairAddress = result - 56;
                        return true;
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean canTickModule() {

        if (lostConnection != null && lostConnection.visible) {
            tryReconnect(lostConnection);
            return false;
        } else if (connecting != null && connecting.visible) {
            return false;
        } else if (isDead()) {
            tryRevive();

            if (revives > main.config.MAX_DEATHS) {
                main.setRunning(false);
            }

            return false;
        } else if (isInvalidShip()) {

            if (System.currentTimeMillis() - validTime > 90 * 1000) {
                API.refresh();
                validTime = System.currentTimeMillis();
            }

            System.out.println("Is invalid!");

        } else {
            validTime = System.currentTimeMillis();
        }

        return true;
    }

}
