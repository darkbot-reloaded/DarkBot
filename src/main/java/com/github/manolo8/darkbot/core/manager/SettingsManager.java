package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.itf.Tickable;
import eu.darkbot.api.API;

import static com.github.manolo8.darkbot.Main.API;


public class SettingsManager implements Manager, Tickable, API.Singleton {

    private final Main main;

    private long address;

    public int config;
    public int force2d;
    public int nextMap;
    public int currMap;

    public int enemyCount;
    public boolean attackViaSlotbar;

    public String lang, driver;

    public SettingsManager(Main main) {
        this.main = main;
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.settingsAddress.add(value -> address = value);
    }

    @Override
    public void tick() {
        this.config = API.readMemoryInt(address + 92);

        // x-1 & x-2 maps enemy counter
        this.enemyCount = API.readInt(main.settingsManager.address, 592, 40);
        this.attackViaSlotbar = API.readBoolean(main.settingsManager.address, 164);

        this.nextMap = API.readMemoryInt(address + 244);
        this.currMap = API.readMemoryInt(address + 248);

        this.force2d = API.readMemoryInt(address, 784, 0x20);

        this.lang = API.readMemoryStringFallback(address, null, 640);
        this.driver = API.readString(address, 432);


        // Enforce GPU capabilities support
//        if (main.config.BOT_SETTINGS.API_CONFIG.ENFORCE_HW_ACCEL) {
//            API.replaceInt(address + 332, 0, 1);
//            API.replaceInt(address + 340, 0, 1);
//        }
    }

    public boolean is2DForced() {
        return force2d == 1;
    }
}
