package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.objects.Health;
import com.github.manolo8.darkbot.core.objects.PlayerInfo;
import com.github.manolo8.darkbot.core.utils.TraitPattern;
import com.github.manolo8.darkbot.core.utils.pathfinder.AreaImpl;
import com.github.manolo8.darkbot.core.utils.pathfinder.CircleImpl;
import eu.darkbot.api.objects.EntityInfo;

import java.util.Collection;

import static com.github.manolo8.darkbot.Main.API;

public class BattleStation
        extends Entity
        implements Obstacle, eu.darkbot.api.entities.BattleStation {

    public PlayerInfo info = new PlayerInfo();
    public Health health = new Health();
    public CircleImpl area = new CircleImpl(0, 0, 1200);
    public int hullId;

    public BattleStation(int id, long address) {
        super(id);
        this.update(address);
    }

    public long lockPtr;

    @Override
    public void update() {
        super.update();

        info.update();
        health.update();
        if (locationInfo.isMoving()) {
            area.set(locationInfo.now, 1200);
            ConfigEntity.INSTANCE.updateSafetyFor(this);
        }
    }

    @Override
    public int getHullId() {
        return hullId;
    }

    @Override
    public Lock getLockType() {
        return Lock.of(API.readMemoryInt(lockPtr, 48, 40));
    }

    @Override
    public eu.darkbot.api.objects.Health getHealth() {
        return health;
    }

    @Override
    public EntityInfo getEntityInfo() {
        return info;
    }

    @Override
    public void removed() {
        super.removed();
        ConfigEntity.INSTANCE.updateSafetyFor(this);
    }

    @Override
    public void update(long address) {
        super.update(address);

        hullId = API.readMemoryInt(address + 116);
        info.update(API.readMemoryLong(address + 120));

        health.update(findInTraits(TraitPattern::ofHealth));
        lockPtr = findInTraits(TraitPattern::ofLockType);
    }

    @Override
    public AreaImpl getArea() {
        return area;
    }

    @Override
    public boolean isZoneValid() {
        return use();
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }

    @Override
    public boolean use() {
        return hullId > 0 && hullId < 255 && info.isEnemy();
    }

    @Override
    public String toString() {
        return id + "," + hullId;
    }

    public static class Module
            extends BattleStation
            implements eu.darkbot.api.entities.BattleStation.Module {

        private String moduleId;

        public Module(int id, long address) {
            super(id, address);
        }

        @Override
        public void update(long address) {
            super.update(address);
            this.moduleId = API.readMemoryString(address, 112);
        }

        @Override
        public String getModuleId() {
            return moduleId;
        }
    }
}
