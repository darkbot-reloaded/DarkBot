package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.objects.Health;
import com.github.manolo8.darkbot.core.objects.PlayerInfo;
import com.github.manolo8.darkbot.core.objects.ShipInfo;
import com.github.manolo8.darkbot.utils.MathUtils;

import java.util.HashMap;

import static com.github.manolo8.darkbot.Main.API;

public class Ship extends Entity {

    private static HashMap<Integer, Long> cacheTimer = new HashMap<>();

    public Health health;
    public PlayerInfo playerInfo;
    public ShipInfo shipInfo;
    public boolean invisible;

    public long timer;

    public Ship(int id) {
        super(id);

        this.health = new Health();
        this.playerInfo = new PlayerInfo();
        this.shipInfo = new ShipInfo();

        Long temp = cacheTimer.remove(id);

        if (temp != null) timer = temp;
    }

    public boolean isAttacking(Ship other) {
        return shipInfo.target == other.address;
    }

    public boolean isAiming(Ship other) {
        return MathUtils.angleDiff(shipInfo.angle,locationInfo.now.angle(other.locationInfo.now)) < 0.2;
    }

    @Override
    public void update() {
        super.update();
        clickable.update();

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

    @Override
    public void removed() {
        super.removed();

        if (isInTimer()) {
            cacheTimer.put(id, timer);
        }
    }

    public void setTimerTo(long time) {
        timer = System.currentTimeMillis() + time;

        clearIgnored();
    }

    private void clearIgnored() {
        if (cacheTimer.size() > 10) {
            cacheTimer.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis());
        }
    }

    public boolean isInTimer() {
        return timer > System.currentTimeMillis();
    }
}
