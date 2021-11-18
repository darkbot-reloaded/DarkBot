package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.objects.Health;
import com.github.manolo8.darkbot.core.objects.PlayerInfo;
import com.github.manolo8.darkbot.core.objects.ShipInfo;
import com.github.manolo8.darkbot.core.utils.TraitPattern;
import com.github.manolo8.darkbot.utils.MathUtils;
import eu.darkbot.api.game.entities.Pet;
import eu.darkbot.api.game.items.SelectableItem;
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

    private com.github.manolo8.darkbot.core.entities.Pet pet;

    public int formationId;
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
        clickable.update();

        health.update();
        shipInfo.update();
        playerInfo.update();
        attackTarget.update();

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
            //if (!laserAttacking) return;

            long entityPtr = API.readMemoryLong(address, 64, 32);

            if (entityPtr == 0 && targetedEntity != null) targetedEntity = null;
            else if (entityPtr != 0 && (targetedEntity == null || entityPtr != targetedEntity.address)) {
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
    public boolean hasPet() {
        return pet != null;
    }

    @Override
    public Optional<Pet> getPet() {
        return Optional.ofNullable(pet);
    }

    @Override
    public SelectableItem.Formation getFormation() {
        return SelectableItem.Formation.of(formationId);
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
    public boolean isAiming(@NotNull Locatable other) {
        return MathUtils.angleDiff(getAngle(), getLocationInfo().angleTo(other)) < 0.2;
    }

    @Override
    public Optional<Location> getDestination() {
        return shipInfo.destination.address == 0 ?
                Optional.empty() : Optional.of(shipInfo.destination);
    }
}
