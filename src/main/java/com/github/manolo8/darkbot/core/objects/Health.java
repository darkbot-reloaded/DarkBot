package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;

import static com.github.manolo8.darkbot.Main.API;

public class Health extends Updatable {

    public int hp;
    public int maxHp;
    public int shield;
    public int maxShield;

    public long lastIncreased;
    public long lastDecreased;

    @Override
    public void update() {

        int hpLast = hp, maxHpLast = maxHp;

        hp = readIntFromIntHolder(48);
        maxHp = readIntFromIntHolder(56);
        shield = readIntFromIntHolder(80);
        maxShield = readIntFromIntHolder(88);

        if (maxHpLast != maxHp) return;
        if (hpLast > hp) {
            lastDecreased = System.currentTimeMillis();
        } else if (hpLast < hp) {
            lastIncreased = System.currentTimeMillis();
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

    public boolean isDecreasedIn(int time) {
        return System.currentTimeMillis() - lastDecreased < time;
    }

    public boolean isIncreasedIn(int time) {
        return System.currentTimeMillis() - lastIncreased < time;
    }
}
