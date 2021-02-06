package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.itf.HealthHolder;

import static com.github.manolo8.darkbot.Main.API;

public class Health extends Updatable implements HealthHolder, eu.darkbot.api.objects.Health {

    public int hp;
    public int maxHp;
    public int hull;
    public int maxHull;
    public int shield;
    public int maxShield;

    private long hpLastIncreased, hpLastDecreased,
            hullLastIncreased, hullLastDecreased,
            shieldLastIncreased, shieldLastDecreased;

    @Override
    public void update() {
        int hpLast = hp, maxHpLast = maxHp,
                hullLast = hp, maxHullLast = maxHp,
                shieldLast = shield, maxShieldLast = maxShield;

        hp = readIntFromIntHolder(48);
        maxHp = readIntFromIntHolder(56);
        hull = readIntFromIntHolder(64);
        maxHull  = readIntFromIntHolder(72);
        shield = readIntFromIntHolder(80);
        maxShield = readIntFromIntHolder(88);

        if (maxHpLast == maxHp && hpLast != hp) {
            if (hpLast > hp) hpLastDecreased = System.currentTimeMillis();
            else hpLastIncreased = System.currentTimeMillis();
        }
        if (maxHullLast == maxHull && hullLast != hull) {
            if (hullLast > hull) hullLastDecreased = System.currentTimeMillis();
            else hullLastIncreased = System.currentTimeMillis();
        }
        if (maxShieldLast == maxShield && shieldLast != shield) {
            if (shieldLast > shield) shieldLastDecreased = System.currentTimeMillis();
            else shieldLastIncreased = System.currentTimeMillis();
        }
    }

    private int readIntFromIntHolder(int holderOffset) {
        return API.readMemoryInt(API.readMemoryLong(address + holderOffset) + 40);
    }

    public double hpPercent() {
        return maxHp == 0 ? 1 : ((double) hp / (double) maxHp);
    }

    public double shieldPercent() {
        return maxShield == 0 ? 1 : ((double) shield / (double) maxShield);
    }

    public boolean hpDecreasedIn(int time) {
        return System.currentTimeMillis() - hpLastDecreased < time;
    }

    public boolean hpIncreasedIn(int time) {
        return System.currentTimeMillis() - hpLastIncreased < time;
    }

    public boolean hullDecreasedIn(int time) {
        return System.currentTimeMillis() - hullLastDecreased < time;
    }

    public boolean hullIncreasedIn(int time) {
        return System.currentTimeMillis() - hullLastIncreased < time;
    }

    public boolean shDecreasedIn(int time) {
        return System.currentTimeMillis() - shieldLastDecreased < time;
    }

    public boolean shIncreasedIn(int time) {
        return System.currentTimeMillis() - shieldLastIncreased < time;
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

    @Override
    public boolean shieldDecreasedIn(int time) {
        return shDecreasedIn(time);
    }

    @Override
    public boolean shieldIncreasedIn(int time) {
        return shIncreasedIn(time);
    }
}
