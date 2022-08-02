package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import static com.github.manolo8.darkbot.Main.API;

public class AstralGateProxy extends Updatable {
    public int highScore, currentRift, currentScore, cpuCount;

    @Override
    public void update() {
        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;

        highScore = API.readMemoryInt(data + 64);
        currentRift = API.readMemoryInt(API.readMemoryLong(data + 80) + 40);
        currentScore = API.readMemoryInt(API.readMemoryLong(data + 88) + 40);
        cpuCount = API.readMemoryInt(API.readMemoryLong(data + 96) + 40);

    }
}
