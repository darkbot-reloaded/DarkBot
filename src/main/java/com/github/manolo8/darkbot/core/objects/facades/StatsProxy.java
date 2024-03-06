package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import eu.darkbot.api.API;

public class StatsProxy extends Updatable implements API.Singleton {
    private int fps, memory;

    public StatsProxy() {
    }

    @Override
    public void update() {
        this.fps = readInt(80, 32);
        this.memory = (int) readDouble(80, 56);
    }

    public int getFps() {
        return fps;
    }

    public int getMemory() {
        return memory;
    }
}