package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import static com.github.manolo8.darkbot.Main.API;

public class WorldBossOverviewProxy extends Updatable {
    int attempts = 0;

    @Override
    public void update() {
        if (address == 0) return;
        long data = API.readMemoryLong(address + 0x30) & ByteUtils.ATOM_MASK;
        long attemptsData = API.readMemoryLong(data + 0x60) & ByteUtils.ATOM_MASK;

        this.attempts = API.readInt(attemptsData + 0x28);
    }

    public int getAttempts() {
        return attempts;
    }
}
