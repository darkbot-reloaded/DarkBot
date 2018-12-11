package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.def.Updatable;

import static com.github.manolo8.darkbot.Main.API;

public class Health implements Updatable {

    private long address;

    public int hp;
    public int maxHp;
    public int shield;
    public int maxShield;

    public long lastIncreased;
    public long lastDecreased;

    public Health() {
    }

    public Health(long address) {
        this.address = address;
    }

    @Override
    public void update() {
        int hpLast = hp;

        hp = API.readMemoryInt(API.readMemoryLong(address + 48) + 40);
        maxHp = API.readMemoryInt(API.readMemoryLong(address + 56) + 40);
        shield = API.readMemoryInt(API.readMemoryLong(address + 80) + 40);
        maxShield = API.readMemoryInt(API.readMemoryLong(address + 88) + 40);

        if (hpLast > hp) {
            //Decreased
            lastDecreased = System.currentTimeMillis();
        } else if (hpLast < hp) {
            //Increased
            lastIncreased = System.currentTimeMillis();
        }
    }

    public double healthPercent() {
        return ((double) hp / (double) maxHp);
    }

    public double shieldPercent() {
        return ((double) shield / (double) maxShield);
    }

    public boolean isLoaded() {
        return hp != 0 || maxHp != 0;
    }

    @Override
    public void update(long address) {
        this.address = address;
    }

    public boolean isDecreasedIn(int time) {
        //5 - 3 = 2 -> 2 > 5
        return System.currentTimeMillis() - lastDecreased < time;
    }

    public boolean isIncreasedIn(int time) {
        return System.currentTimeMillis() - lastIncreased < time;
    }
}
