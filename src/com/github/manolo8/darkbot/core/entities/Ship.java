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

    public Ship() {
        this.health = new Health();
        this.playerInfo = new PlayerInfo();
        this.shipInfo = new ShipInfo();
    }

    public Ship(long address, int id) {
        super(address, id);

        this.health = new Health(API.readMemoryLong(address + 184));
        this.playerInfo = new PlayerInfo(API.readMemoryLong(address + 248));
        this.shipInfo = new ShipInfo(API.readMemoryLong(address + 232));
    }

    public boolean isEnemy() {
        return playerInfo.factionId != instance.playerInfo.factionId && playerInfo.clanDiplomacy != 1 && playerInfo.clanDiplomacy != 2 || playerInfo.clanDiplomacy == 3;
    }

    public boolean isAttacking(Ship other) {
        return shipInfo.target == other.address;
    }

    @Override
    public void update() {
        super.update();

        health.update();
        shipInfo.update();

        invisible = API.readMemoryBoolean(API.readMemoryLong(address + 160) + 32);

        //playerInfo unique update!
    }

    @Override
    public void update(long address) {
        super.update(address);

        playerInfo.update(API.readMemoryLong(address + 248));
        health.update(API.readMemoryLong(address + 184));
        shipInfo.update(API.readMemoryLong(address + 232));

//        System.out.println(getClass().getSimpleName() + " -> " + (address + 248));
    }
}
