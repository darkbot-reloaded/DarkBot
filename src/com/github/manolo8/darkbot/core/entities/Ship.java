package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.objects.Health;
import com.github.manolo8.darkbot.core.objects.PlayerInfo;
import com.github.manolo8.darkbot.core.objects.ShipInfo;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.core.manager.HeroManager.instance;

public class Ship extends Entity {

    public Health health;
    public PlayerInfo playerInfo;
    public ShipInfo shipInfo;
    public boolean invisible;

    public Ship(int id) {
        super(id);

        this.health = new Health();
        this.playerInfo = new PlayerInfo();
        this.shipInfo = new ShipInfo();
    }

    public boolean isAttacking(Ship other) {
        return shipInfo.target == other.address;
    }

    @Override
    public void update() {
        super.update();

        health.update();
        shipInfo.update();
        playerInfo.update();

        invisible = API.readMemoryBoolean(API.readMemoryLong(address + 160) + 32);
    }

    @Override
    public void update(long address) {
        super.update(address);

        playerInfo.update(API.readMemoryLong(address + 248));
        health.update(API.readMemoryLong(address + 184));
        shipInfo.update(API.readMemoryLong(address + 232));
    }
}
