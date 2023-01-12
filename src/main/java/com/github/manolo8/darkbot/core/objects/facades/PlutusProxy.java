package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import static com.github.manolo8.darkbot.Main.API;

public class PlutusProxy extends Updatable implements eu.darkbot.api.API.Singleton {
    public String status;

    @Override
    public void update() {
        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;
        long plutusVoAddr = API.readMemoryLong(data + 0x58) & ByteUtils.ATOM_MASK;

        this.status = API.readMemoryString(API.readMemoryLong(plutusVoAddr+0x30));
    }
}
