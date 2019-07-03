package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;

import static com.github.manolo8.darkbot.Main.API;

public class ShipInfo extends Updatable {

    public int speed;
    public long target;
    private long keepTargetTime;

    @Override
    public void update() {
        long newTarget = API.readMemoryLong(address + 112);
        if (newTarget != 0 || keepTargetTime > System.currentTimeMillis()) {
            target = newTarget;
            if (target != 0) keepTargetTime = System.currentTimeMillis() + 500;
        }
        speed = API.readMemoryInt(API.readMemoryLong(address + 72) + 40);
    }

}
