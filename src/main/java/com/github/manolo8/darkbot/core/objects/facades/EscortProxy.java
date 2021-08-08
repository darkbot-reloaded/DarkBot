package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.managers.EscortAPI;

import static com.github.manolo8.darkbot.Main.API;

public class EscortProxy extends Updatable implements EscortAPI {
    public double time;
    public int keys;

    public void update() {
        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;

        this.keys = API.readMemoryInt(API.readMemoryLong(data + 88) + 40);
        this.time = API.readMemoryDouble(API.readMemoryLong(data + 72) + 56);
    }

    @Override
    public double getTime() {
        return time;
    }

    @Override
    public double getKeys() {
        return keys;
    }
}
