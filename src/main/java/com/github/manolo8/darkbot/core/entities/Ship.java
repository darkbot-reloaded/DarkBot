package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.Health;
import com.github.manolo8.darkbot.core.objects.PlayerInfo;
import com.github.manolo8.darkbot.core.objects.ShipInfo;
import com.github.manolo8.darkbot.core.utils.TraitPattern;
import com.github.manolo8.darkbot.utils.MathUtils;
import eu.darkbot.api.game.other.EntityInfo;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

import static com.github.manolo8.darkbot.Main.API;

public class Ship extends Entity implements eu.darkbot.api.game.entities.Ship {

    private static final HashMap<Integer, Long> cacheTimer = new HashMap<>();

    public Health health         = new Health();
    public PlayerInfo playerInfo = new PlayerInfo();
    public ShipInfo shipInfo     = new ShipInfo(this.locationInfo);

    public int formationId; // later move it up to Player.class
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
        return isAiming((Locatable) other);
    }

    private final Target attackTarget = new Target();
    private long lockPtr;

    @Override
    public void update() {
        super.update();

        health.update();
        shipInfo.update();
        playerInfo.update();
        attackTarget.update();

        formationId = API.readMemoryInt(address, 280, 40, 40);
        invisible = API.readMemoryBoolean(API.readMemoryLong(address + 160) + 32);
    }

    @Override
    public void update(long address) {
        super.update(address);

        playerInfo.update(API.readMemoryLong(address + 248));
        health.update(API.readMemoryLong(address + 184));
        shipInfo.update(API.readMemoryLong(address + 232));

        attackTarget.update(findInTraits(ptr -> API.readMemoryString(ptr, 48, 32).equals("attackLaser")));

        lockPtr = findInTraits(TraitPattern::ofLockType);
    }

    @Override
    public void removed() {
        super.removed();

        if (isInTimer()) {
            cacheTimer.put(id, timer);
        }
    }

    public long timeTo(double distance) {
        return eu.darkbot.api.game.entities.Ship.super.timeTo(distance);
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


    private class Target extends Updatable {
        private Entity targetedEntity;
        private boolean laserAttacking;

        @Override
        public void update() {
            laserAttacking = API.readMemoryLong(address + 64) != 0;
            //if (!laserAttacking) return;

            long entityPtr = API.readMemoryLong(address, 64, 32);

            if (entityPtr == 0) targetedEntity = null;
            else if (targetedEntity == null || entityPtr != targetedEntity.address) {
                if (entityPtr == main.hero.address) {
                    targetedEntity = main.hero;
                    return;
                           //entity ptr can't be assigned to 0 here so don't need that check
                } else if (/*main.hero.pet.address != 0 && */entityPtr == main.hero.pet.address) {
                    targetedEntity = main.hero.pet;
                    return;
                }

                targetedEntity = main.mapManager.entities.allEntities.stream()
                        .flatMap(Collection::stream)
                        .filter(entity -> entity.address == entityPtr)
                        .findAny().orElse(null);
            }
        }
    }

    @Override
    public boolean isInvisible() {
        return invisible;
    }

    @Override
    public boolean isBlacklisted() {
        return isInTimer();
    }

    @Override
    public void setBlacklisted(long time) {
        setTimerTo(time);
    }

    @Override
    public Lock getLockType() {
        return Lock.of(API.readMemoryInt(lockPtr, 48, 40));
    }

    @Override
    public eu.darkbot.api.game.other.Health getHealth() {
        return health;
    }

    @Override
    public EntityInfo getEntityInfo() {
        return playerInfo;
    }

    @Override
    public eu.darkbot.api.game.entities.@Nullable Entity getTarget() {
        return attackTarget.targetedEntity;
    }

    @Override
    public boolean isAttacking() {
        return attackTarget.laserAttacking;
    }

    @Override
    public int getSpeed() {
        return (int) shipInfo.getSpeed();
    }

    @Override
    public double getAngle() {
        return shipInfo.angle;
    }

    @Override
    public double getDestinationAngle() {
        return shipInfo.destinationAngle;
    }

    @Override
    public boolean isAiming(@NotNull Locatable other) {
        return MathUtils.angleDiff(getAngle(), getLocationInfo().angleTo(other)) < 0.2;
    }

    @Override
    public Optional<Location> getDestination() {
        return shipInfo.getDestination();
    }
}
