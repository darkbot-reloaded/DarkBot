package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.itf.NativeUpdatable;
import com.github.manolo8.darkbot.core.itf.Tickable;
import eu.darkbot.api.API;

import static com.github.manolo8.darkbot.Main.API;


public class SettingsManager implements Manager, Tickable, API.Singleton, NativeUpdatable {

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
        this.config = readInt(0x5c);

        // x-1 & x-2 maps enemy counter
        this.enemyCount = readInt(0x258, 40);
        this.attackViaSlotbar = readBoolean(0xa4);

        this.nextMap = readInt(0xf4);
        this.currMap = readInt(0xf8);

        this.force2d = readInt(0x318, 32);

        this.lang = readString(0x288);

        this.hudWrapper = readLong(0x360);
        this.uiWrapper = readLong(0x368);

        // Enforce GPU capabilities support - it still may be an issue on Windows & 2D mode
        if (is2DForced() && main.config.BOT_SETTINGS.API_CONFIG.ENFORCE_HW_ACCEL) {
            replaceInt(0, 1, 0x14c);
            replaceInt(0, 1, 0x154);
        }

        if (!driverNamePrinted) {
            this.driver = readString(0x1b8);

            if (driver != null && !driver.isEmpty()) {
                System.out.println("Game is using: " + driver + " | force2d: " + force2d);
                driverNamePrinted = true;
            }
        }
    }

    @Override
    public int modifyOffset(int offset) {
        if (offset >= 0x188) offset += 8; // 19.07.2023 - jumpGateResourceHash
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

    @Override
    public long getAddress() {
        return address;
    }
}
