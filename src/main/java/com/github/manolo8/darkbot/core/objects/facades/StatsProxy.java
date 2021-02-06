package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.manager.PingManager;
import eu.darkbot.api.managers.PerformanceAPI;

import static com.github.manolo8.darkbot.Main.API;

public class StatsProxy extends Updatable implements PerformanceAPI {
    private final PingManager pingManager;
    private int fps, memory;

    public StatsProxy(PingManager pingManager) {
        this.pingManager = pingManager;
    }

    @Override
    public void update() {
        this.fps = API.readMemoryInt(address, 80, 32);
        this.memory = (int) API.readMemoryDouble(address, 80, 56);
    }

    @Override
    public int getFps() {
        return fps;
    }

    @Override
    public int getMemory() {
        return memory;
    }

    @Override
    public int getPing() {
        return pingManager.ping;
    }

}