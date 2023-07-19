package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.itf.Tickable;
import eu.darkbot.api.API;

import static com.github.manolo8.darkbot.Main.API;


public class SettingsManager implements Manager, Tickable, API.Singleton {

    private final Main main;
    public int config;
    public int force2d;
    public int nextMap;
    public int currMap;
    public int enemyCount;
    public boolean attackViaSlotbar;
    public String lang, driver;
    private long address;
    private boolean driverNamePrinted;
    private long hudWrapper;
    private long uiWrapper;

    public SettingsManager(Main main) {
        this.main = main;
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.settingsAddress.add(value -> {
            force2d = 0;
            address = value;
            driverNamePrinted = false;
        });
    }

    @Override
    public void tick() {
        this.config = API.readMemoryInt(address + convertOffset(92));

        // x-1 & x-2 maps enemy counter
        this.enemyCount = API.readInt(address, convertOffset(600), 40);
        this.attackViaSlotbar = API.readBoolean(address, convertOffset(164));

        this.nextMap = API.readMemoryInt(address + convertOffset(244));
        this.currMap = API.readMemoryInt(address + convertOffset(248));

        this.force2d = API.readMemoryInt(address, convertOffset(792), 0x20);

        this.lang = API.readMemoryStringFallback(address, null, convertOffset(648));

        this.uiWrapper = Main.API.readLong(address, convertOffset(872));
        this.hudWrapper = Main.API.readLong(address, convertOffset(864));

        // Enforce GPU capabilities support - it still may be an issue on Windows & 2D mode
        if (is2DForced() && main.config.BOT_SETTINGS.API_CONFIG.ENFORCE_HW_ACCEL) {
            API.replaceInt(address + convertOffset(332), 0, 1);
            API.replaceInt(address + convertOffset(340), 0, 1);
        }

        if (!driverNamePrinted) {
            this.driver = API.readString(address, "", convertOffset(440));

            if (driver != null && !driver.isEmpty()) {
                System.out.println("Game is using: " + driver + " | force2d: " + force2d);
                driverNamePrinted = true;
            }
        }
    }

    // offsets in settings manager often changes, so there can easily add or remove field offsets
    public static int convertOffset(int offset) {
        if (offset >= 392) offset += 8; // 19.07.2023 - jumpGateResourceHash
        return offset;
    }

    public boolean is2DForced() {
        return force2d == 1;
    }

    public boolean isUiVisible() {
        return API.readBoolean(uiWrapper + 32);
    }

    public void setUiVisible(boolean visible) {
        if (uiWrapper != 0) {
            API.callMethodAsync(4, uiWrapper, visible ? 1 : 0);
        }
    }

    public boolean isHudVisible() {
        return API.readBoolean(hudWrapper + 32);
    }

    public void setHudVisible(boolean visible) {
        if (hudWrapper != 0) {
            API.callMethodAsync(4, hudWrapper, visible ? 1 : 0);
        }
    }
}
