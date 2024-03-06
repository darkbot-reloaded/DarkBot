package com.github.manolo8.darkbot.core.objects.group;

import com.github.manolo8.darkbot.core.objects.Health;

import static com.github.manolo8.darkbot.Main.API;

public class MemberInfo extends Health implements eu.darkbot.api.game.group.MemberInfo {
    public int shipType;
    public String username;

    @Override
    public void update() {
        int hpLast = hp, maxHpLast = maxHp,
                hullLast = hp, maxHullLast = maxHp,
                shieldLast = shield, maxShieldLast = maxShield;

        shipType  = API.readInt(address + 0x20);
        hp        = API.readInt(address + 0x24);
        maxHp     = API.readInt(address + 0x28);
        hull      = API.readInt(address + 0x2C);
        maxHull   = API.readInt(address + 0x30);
        shield    = API.readInt(address + 0x34);
        maxShield = API.readInt(address + 0x38);
        username  = API.readString(API.readLong(address + 0x40));

        checkHealth(hpLast, maxHpLast,
                hullLast, maxHullLast,
                shieldLast, maxShieldLast);
    }

    @Override
    public void update(long address) {
        super.update(address);
        update();
    }

    @Override
    public int getShipType() {
        return shipType;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
