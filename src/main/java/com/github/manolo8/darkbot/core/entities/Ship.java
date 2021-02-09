package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.objects.Health;
import com.github.manolo8.darkbot.core.objects.PlayerInfo;
import com.github.manolo8.darkbot.core.objects.ShipInfo;
import com.github.manolo8.darkbot.utils.MathUtils;
import eu.darkbot.api.entities.Pet;
import eu.darkbot.api.entities.other.Formation;
import eu.darkbot.api.entities.utils.Attackable;
import eu.darkbot.api.objects.EntityInfo;
import eu.darkbot.api.objects.Locatable;
import eu.darkbot.api.objects.Location;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

import static com.github.manolo8.darkbot.Main.API;

public class Ship extends Entity implements eu.darkbot.api.entities.Ship {

    private static HashMap<Integer, Long> cacheTimer = new HashMap<>();

    public Health health         = new Health();
    public PlayerInfo playerInfo = new PlayerInfo();
    public ShipInfo shipInfo     = new ShipInfo();

    private com.github.manolo8.darkbot.core.entities.Pet pet;

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

    private final Target target = new Target();
    private long lockPtr;

    private int formationId;
    @Override
    public void update() {
        super.update();
        clickable.update();

        health.update();
        shipInfo.update();
        playerInfo.update();
        target.update();

        formationId = API.readMemoryInt(address, 280, 40, 40);
        invisible = API.readMemoryBoolean(API.readMemoryLong(address + 160) + 32);

        if (this instanceof HeroManager) return;

        long petAddress = API.readMemoryLong(address + 176);
        if (petAddress != 0 && (pet == null || petAddress != pet.address))
            pet = main.mapManager.entities.pets.stream()
                    .filter(p -> p.address == petAddress)
                    .findAny().orElse(null);
    }

    @Override
    public void update(long address) {
        super.update(address);

        playerInfo.update(API.readMemoryLong(address + 248));
        health.update(API.readMemoryLong(address + 184));
        shipInfo.update(API.readMemoryLong(address + 232));

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
                } else if (main.hero.pet.address != 0 && entityPtr == main.hero.pet.address) {
                    targetedEntity = main.hero.pet;
                    return;
                }

                targetedEntity = main.mapManager.entities.allEntities.stream()
                        .flatMap(Collection::stream)
                        .filter(entity -> entity.address == entityPtr)
                        .findAny().orElse(null);
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
    public void setBlacklisted(long forTime) {
        setTimerTo(forTime);
    }

    @Override
    public boolean hasPet() {
        return pet != null;
    }

    @Override
    public Optional<Pet> getPet() {
        return Optional.ofNullable(pet);
    }

    @Override
    public Formation getFormation() {
        return Formation.of(formationId);
    }

    @Override
    public boolean isInFormation(int formationId) {
        return formationId == this.formationId;
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
        return playerInfo;
    }

    @Override
    public eu.darkbot.api.entities.@Nullable Entity getTarget() {
        return target.targetedEntity;
    }

    @Override
    public boolean isAttacking() {
        return target.laserAttacking;
    }

    @Override
    public boolean isAttacking(Attackable other) {
        return other == target.targetedEntity;
    }

    @Override
    public int getSpeed() {
        return shipInfo.speed == 0 ? (int) locationInfo.speed : shipInfo.speed;
    }

    @Override
    public double getAngle() {
        return shipInfo.angle;
    }

    @Override
    public boolean isAiming(Locatable other) {
        return MathUtils.angleDiff(shipInfo.angle, getLocationInfo().angleTo(other)) < 0.2;
    }

    @Override
    public Optional<Location> getDestination() {
        if (shipInfo.destination.address == 0) return Optional.empty();
        return Optional.of(shipInfo.destination);
    }
}
