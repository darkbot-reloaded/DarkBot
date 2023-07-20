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
        this.config = readInt(92);

        // x-1 & x-2 maps enemy counter
        this.enemyCount = readInt(600, 40);
        this.attackViaSlotbar = readBoolean(164);

        this.nextMap = readInt(244);
        this.currMap = readInt(248);

        this.force2d = readInt(792, 32);

        this.lang = readString(648);

        this.uiWrapper = readLong(872);
        this.hudWrapper = readLong(864);

        // Enforce GPU capabilities support - it still may be an issue on Windows & 2D mode
        if (is2DForced() && main.config.BOT_SETTINGS.API_CONFIG.ENFORCE_HW_ACCEL) {
            replaceInt(0, 1, 332);
            replaceInt(0, 1, 340);
        }

        if (!driverNamePrinted) {
            this.driver = readString(440);

            if (driver != null && !driver.isEmpty()) {
                System.out.println("Game is using: " + driver + " | force2d: " + force2d);
                driverNamePrinted = true;
            }
        }
    }

    @Override
    public int modifyOffset(int offset) {
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

    @Override
    public long getAddress() {
        return address;
    }
}
