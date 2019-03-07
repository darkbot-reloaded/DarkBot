package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;

import static com.github.manolo8.darkbot.Main.API;

public class Health extends Updatable {

    public int hp;
    public int maxHp;
    public int shield;
    public int maxShield;

    private long hpLastIncreased, hpLastDecreased, shieldLastIncreased, shieldLastDecreased;

    @Override
    public void update() {

        int hpLast = hp, maxHpLast = maxHp, shieldLast = shield, maxShieldLast = maxShield;

        hp = readIntFromIntHolder(48);
        maxHp = readIntFromIntHolder(56);
        shield = readIntFromIntHolder(80);
        maxShield = readIntFromIntHolder(88);

        if (maxHpLast == maxHp && hpLast != hp) {
            if (hpLast > hp) hpLastDecreased = System.currentTimeMillis();
            else hpLastIncreased = System.currentTimeMillis();
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


    public boolean shDecreasedIn(int time) {
        return System.currentTimeMillis() - shieldLastDecreased < time;
    }

    public boolean shIncreasedIn(int time) {
        return System.currentTimeMillis() - shieldLastIncreased < time;
    }

}
