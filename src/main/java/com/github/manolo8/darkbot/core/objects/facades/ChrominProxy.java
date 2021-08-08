package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.managers.ChrominAPI;

import static com.github.manolo8.darkbot.Main.API;

public class ChrominProxy extends Updatable implements ChrominAPI {

    public double currAmt, maxAmt;

    @Override
    public void update() {
        if (address == 0) return;

        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;

        this.maxAmt = API.readMemoryDouble(data + 48);
        this.currAmt = API.readMemoryDouble(data + 40);
    }

    @Override
    public double getCurrentAmount() {
        return currAmt;
    }

    @Override
    public double getMaxAmount() {
        return maxAmt;
    }
}
