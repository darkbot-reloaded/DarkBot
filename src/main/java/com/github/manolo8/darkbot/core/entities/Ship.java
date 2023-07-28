package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.Main;
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

    private Lock lockType = Lock.UNKNOWN;

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

    @Override
    public void added(Main main) {
        super.added(main);
        attackTarget.added(main);
    }

    public boolean isAttacking(Ship other) {
        return shipInfo.target == other.address;
    }

    public boolean isAiming(Ship other) {
        return isAiming((Locatable) other);
    }

    protected final Target attackTarget = new Target();
    private long lockPtr;
    private int shipId;

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

        shipId = API.readInt(address, 192, 76);
        lockType = Lock.of(API.readMemoryInt(lockPtr, 48, 40));
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

//    // implementation of fast select & instant attack
//    @Override
//    public boolean trySelect(boolean tryAttack) {
//        if (!isSelectable() || distanceTo(main.hero) > DEFAULT_CLICK_RADIUS) return false;
//
//        API.writeMemoryLong(API.readMemoryLong(main.mapManager.mapAddress, 120) + 40, address);
//        if (tryAttack)
//            main.facadeManager.settings.pressKeybind(SettingsProxy.KeyBind.ATTACK_LASER);
//
//        return true;
//    }

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


    public static class Target extends Updatable {
        public Entity targetedEntity;
        public boolean laserAttacking;
        private Main main;

        public void added(Main main) {
            this.main = main;
        }

        @Override
        public void update() {
            long targetPtr = API.readMemoryLong(address + 64);
            laserAttacking = targetPtr != 0;

            long entityPtr = laserAttacking ? API.readMemoryLong(targetPtr, 32) : 0;

            if (entityPtr == 0) targetedEntity = null;
            else if (targetedEntity == null || entityPtr != targetedEntity.address) {
                targetedEntity = main.mapManager.entities.findEntityByAddress(entityPtr);
            }
        }
    }

    @Override
    public int getShipId() {
        return shipId;
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
        return lockType;
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
    public boolean isMoving() {
        return shipInfo.isMoving();
    }

    @Override
    public boolean isMoving(long inTime) {
        return shipInfo.isMoving(inTime);
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

    @Override
    public String toString() {
        return super.toString() + " I:" + getShipId() + " S:" + getSpeed() + " " + getLockType();
    }
}
