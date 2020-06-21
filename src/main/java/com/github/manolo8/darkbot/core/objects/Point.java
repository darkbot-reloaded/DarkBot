package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;

import static com.github.manolo8.darkbot.Main.API;

public class Point extends UpdatableAuto {

    public double x;
    public double y;

    public void update() {
        if (address == 0) return;
        this.x = API.readMemoryDouble(address + 32);
        this.y = API.readMemoryDouble(address + 40);
    }
}
