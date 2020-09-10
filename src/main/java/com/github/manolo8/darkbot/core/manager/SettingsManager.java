package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.itf.Tickable;

import static com.github.manolo8.darkbot.Main.API;


public class SettingsManager implements Manager, Tickable {

    private long address;

    public int config;
    public int force2d;
    public int nextMap;
    public int currMap;

    public String lang;

    public SettingsManager(Main main) {}

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.settingsAddress.add(value -> address = value);
    }

    @Override
    public void tick() {
        this.config = API.readMemoryInt(address + 0x34);

        this.nextMap = API.readMemoryInt(address + 0xCC);
        this.currMap = API.readMemoryInt(address + 0xD0);

        this.force2d = API.readMemoryInt(address, 0x2D0, 0x20);

        this.lang = API.readMemoryString(address, 0x258);
    }


}
