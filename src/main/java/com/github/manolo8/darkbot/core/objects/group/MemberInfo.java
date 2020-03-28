package com.github.manolo8.darkbot.core.objects.group;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.itf.HealthHolder;

import static com.github.manolo8.darkbot.Main.API;

public class MemberInfo extends UpdatableAuto implements HealthHolder {
    public int shipType;
    public int hp;
    public int maxHp;
    public int hull;
    public int maxHull;
    public int shield;
    public int maxShield;
    public String username;

    @Override
    public void update() {
        shipType  = API.readMemoryInt(address + 0x20);
        hp        = API.readMemoryInt(address + 0x24);
        maxHp     = API.readMemoryInt(address + 0x28);
        hull      = API.readMemoryInt(address + 0x2C);
        maxHull   = API.readMemoryInt(address + 0x30);
        shield    = API.readMemoryInt(address + 0x34);
        maxShield = API.readMemoryInt(address + 0x38);
        username  = API.readMemoryString(API.readMemoryLong(address + 0x40));
    }

    @Override
    public int getHp() {
        return hp;
    }

    @Override
    public int getMaxHp() {
        return maxHp;
    }

    @Override
    public int getHull() {
        return hull;
    }

    @Override
    public int getMaxHull() {
        return maxHull;
    }

    @Override
    public int getShield() {
        return shield;
    }

    @Override
    public int getMaxShield() {
        return maxShield;
    }
}
