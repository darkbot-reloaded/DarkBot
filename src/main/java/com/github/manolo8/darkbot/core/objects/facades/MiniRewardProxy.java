package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import eu.darkbot.api.API;

import static com.github.manolo8.darkbot.Main.API;

public class MiniRewardProxy extends Updatable implements API.Singleton {
    int dayClaimed;
    boolean claimable;

    @Override
    public void update() {
        if (address == 0) return;

        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;
        this.dayClaimed = API.readMemoryInt(data + 0x40);

        long claimableAddr = API.readMemoryLong(data + 0x50) & ByteUtils.ATOM_MASK;
        this.claimable = API.readBoolean(claimableAddr + 0x20);
    }

    public int getDayClaimed() {
        return dayClaimed;
    }

    public boolean isClaimable() {
        return claimable;
    }
}