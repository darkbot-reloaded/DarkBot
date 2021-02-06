package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.Health;
import com.github.manolo8.darkbot.core.objects.PlayerInfo;
import com.github.manolo8.darkbot.core.utils.pathfinder.AreaImpl;
import com.github.manolo8.darkbot.core.utils.pathfinder.CircleImpl;

import java.util.Collection;

import static com.github.manolo8.darkbot.Main.API;

public class BattleStation
        extends Entity
        implements Obstacle {

    public PlayerInfo info = new PlayerInfo();
    public Health health = new Health();
    public CircleImpl area = new CircleImpl(0, 0, 1200);
    public int hullId;

    public BattleStation(int id, long address) {
        super(id);
        this.update(address);
    }

    public Target target = new Target();
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

        target.update();
    }

    public class Target extends Updatable {
        public Entity targetedEntity;

        public boolean laserAttacking;

        @Override
        public void update() {
            laserAttacking = API.readMemoryLong(address + 64) != 0;
            if (!laserAttacking) return;

            long entityPtr = API.readMemoryLong(address, 64, 32);

            if (entityPtr == 0 && targetedEntity != null) targetedEntity = null;
            else if (entityPtr != 0 && (targetedEntity == null || entityPtr != targetedEntity.address))
                if (entityPtr == main.hero.address) {
                    targetedEntity = main.hero;
                    return;
                }

            targetedEntity = main.mapManager.entities.allEntities.stream()
                    .flatMap(Collection::stream)
                    .filter(entity -> entity.address == entityPtr)
                    .findAny().orElse(null);
        }

        @Override
        public String toString() {
            return "Target{" +
                    "targetedEntity=" + targetedEntity +
                    ", laserAttacking=" + laserAttacking +
                    '}';
        }
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

        health.update(findInTraits(ptr -> {
            long classType = API.readMemoryLong(ptr, 48, 0x10);

            return API.readMemoryLong(ptr, 48 + 8) == classType &&
                    API.readMemoryLong(ptr, 48 + 8 * 2) == classType &&
                    API.readMemoryLong(ptr, 48 + 8 * 3) == classType &&
                    API.readMemoryLong(ptr, 48 + 8 * 4) == classType &&
                    API.readMemoryLong(ptr, 48 + 8 * 5) == classType;
        }));

        target.update(findInTraits(ptr -> API.readMemoryString(ptr, 48, 32).equals("attackLaser")));

        lockPtr = findInTraits(ptr -> {
            long temp = API.readMemoryLong(ptr + 48);
            int lockType = API.readMemoryInt(temp + 40);

            return (lockType == 1 || lockType == 2 || lockType == 3 || lockType == 4) &&
                    API.readMemoryInt(temp + 32) == Integer.MIN_VALUE &&
                    API.readMemoryInt(temp + 36) == Integer.MAX_VALUE;
        });
    }



    @Override
    public AreaImpl getArea() {
        return area;
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
        return target.toString() + " | " + API.readMemoryLong(lockPtr, 48, 40);
    }



}
