package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.core.utils.Location;
import eu.darkbot.api.objects.Locatable;

public class FakeNpc extends Npc {

    private long pingAlive = 0;

    public FakeNpc(Main main) {
        super(Integer.MIN_VALUE);
        super.removed = true;
        super.address = -1;
        super.main = main;
    }

    @Override
    public boolean isAttacking(Ship other) {
        return false;
    }

    @Override
    public boolean isAiming(Ship other) {
        return false;
    }

    @Override
    public boolean isInvalid(long mapAddress) {
        return false;
    }

    @Override public void update() {}
    @Override public void update(long address) {}

    @Override
    public void removed() {
        super.removed();
        pingAlive = 0;
    }

    public boolean isPingAlive() {
        return pingAlive > System.currentTimeMillis();
    }

    public void set(Location loc, NpcInfo type) {
        if (loc == null || (loc.x == 0 && loc.y == 0)) {
            if (!isPingAlive()) removed();
            return;
        }
        removed = false;
        pingAlive = System.currentTimeMillis() + 2_000;
        locationInfo.updatePosition(loc.x, loc.y);
        npcInfo = type;
    }

}
