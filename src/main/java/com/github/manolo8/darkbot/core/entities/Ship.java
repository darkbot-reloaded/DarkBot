package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.Health;
import com.github.manolo8.darkbot.core.objects.PlayerInfo;
import com.github.manolo8.darkbot.core.objects.ShipInfo;
import com.github.manolo8.darkbot.utils.MathUtils;

import java.util.Collection;
import java.util.HashMap;

import static com.github.manolo8.darkbot.Main.API;

public class Ship extends Entity {

    private static HashMap<Integer, Long> cacheTimer = new HashMap<>();

    public Health health         = new Health();
    public PlayerInfo playerInfo = new PlayerInfo();
    public ShipInfo shipInfo     = new ShipInfo();

    public boolean invisible;
    public long timer;

    public Ship() {}

    public Ship(int id) {
        super(id);

        Long temp = cacheTimer.remove(id);
        if (temp != null) timer = temp;
    }

    public Ship(int id, long address) {
        super(id);
        this.update(address);

        Long temp = cacheTimer.remove(id);
        if (temp != null) timer = temp;
    }

    public boolean isAttacking(Ship other) {
        return shipInfo.target == other.address;
    }

    public boolean isAiming(Ship other) {
        return MathUtils.angleDiff(shipInfo.angle, locationInfo.now.angle(other.locationInfo.now)) < 0.2;
    }

    //private Target target = new Target(); to implement in attackableImpl
    //private long lockPtr;

    @Override
    public void update() {
        super.update();
        clickable.update();

        health.update();
        shipInfo.update();
        playerInfo.update();
        //target.update();

        invisible = API.readMemoryBoolean(API.readMemoryLong(address + 160) + 32);
    }

    @Override
    public void update(long address) {
        super.update(address);

        playerInfo.update(API.readMemoryLong(address + 248));
        health.update(API.readMemoryLong(address + 184));
        shipInfo.update(API.readMemoryLong(address + 232));

        /*target.update(findInTraits(ptr -> API.readMemoryString(ptr, 48, 32).equals("attackLaser")));

        lockPtr = findInTraits(ptr -> {
            long temp = API.readMemoryLong(ptr + 48);
            int lockType = API.readMemoryInt(temp + 40);

            return (lockType == 1 || lockType == 2 || lockType == 3 || lockType == 4) &&
                    API.readMemoryInt(temp + 32) == Integer.MIN_VALUE &&
                    API.readMemoryInt(temp + 36) == Integer.MAX_VALUE;
        });*/
    }

    @Override
    public void removed() {
        super.removed();

        if (isInTimer()) {
            cacheTimer.put(id, timer);
        }
    }

    public long timeTo(double distance) {
        return (long) (distance * 1000 / shipInfo.speed);
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
    }
}
