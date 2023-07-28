package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.objects.Health;
import com.github.manolo8.darkbot.core.objects.PlayerInfo;
import com.github.manolo8.darkbot.core.utils.TraitPattern;
import com.github.manolo8.darkbot.core.utils.pathfinder.AreaImpl;
import com.github.manolo8.darkbot.core.utils.pathfinder.CircleImpl;
import eu.darkbot.api.game.other.EntityInfo;
import org.jetbrains.annotations.Nullable;

import static com.github.manolo8.darkbot.Main.API;

public class BattleStation
        extends Entity
        implements Obstacle, eu.darkbot.api.game.entities.BattleStation {
    private static final int AVOID_RADIUS = 800;

    public PlayerInfo info = new PlayerInfo();
    public Health health = new Health();
    public CircleImpl area = new CircleImpl(0, 0, AVOID_RADIUS);
    public int hullId;

    protected final Ship.Target target = new Ship.Target();

    public BattleStation(int id, long address) {
        super(id);
        this.update(address);
    }

    public long lockPtr;

    @Override
    public void update() {
        super.update();
        clickable.update();
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
    public eu.darkbot.api.game.other.Health getHealth() {
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
    public AreaImpl getArea() {
        return area;
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }

    @Override
    public boolean use() {
        return false;
    }

    @Override
    public String toString() {
        return id + "," + hullId;
    }

    public static class Asteroid extends BattleStation implements eu.darkbot.api.game.entities.BattleStation.Asteroid {

        public Asteroid(int id, long address) {
            super(id, address);
        }

        @Override
        public void update() {
            super.update();

            if (info.username.isEmpty()) {
                info.username = Main.API.readString(traits.getLast(), "", 56, 40);
            }
        }
    }

    public static class Built extends BattleStation implements eu.darkbot.api.game.entities.BattleStation.Hull {

        private int deflectorId;
        private double deflectorExpansion,hullExpansion;

        public Built(int id, long address) {
            super(id, address);
        }

        @Override
        public void update() {
            super.update();

            info.update();
            health.update();
            if (locationInfo.isMoving()) {
                ConfigEntity.INSTANCE.updateSafetyFor(this);
            }
        }

        @Override
        public void update(long address) {
            super.update(address);

            deflectorId = API.readInt(address + 112);
            hullId = API.readMemoryInt(address + 116);
            info.update(API.readMemoryLong(address + 120));

            deflectorExpansion = API.readDouble(address + 136);
            hullExpansion = API.readDouble(address + 144);

            health.update(findInTraits(TraitPattern::ofHealth));
            lockPtr = findInTraits(TraitPattern::ofLockType);
        }

        @Override
        public double getHullExpansion() {
            return hullExpansion;
        }

        @Override
        public int getDeflectorShieldId() {
            return deflectorId;
        }

        @Override
        public double getDeflectorShieldExpansion() {
            return deflectorExpansion;
        }
    }

    public static class Module
            extends BattleStation
            implements eu.darkbot.api.game.entities.BattleStation.Module {

        private String moduleId;
        private long moduleIdTemp;
        private Type moduleType;

        public Module(int id, long address) {
            super(id, address);
        }

        @Override
        public void update(long address) {
            super.update(address);
            this.moduleIdTemp = API.readLong(address + 112);
            this.moduleId = API.readMemoryString(moduleIdTemp);
            this.moduleType = Type.of(moduleId);

            info.update(API.readMemoryLong(address + 120));
            health.update(findInTraits(TraitPattern::ofHealth));
            lockPtr = findInTraits(TraitPattern::ofLockType);

            target.update(findInTraits(ptr -> API.readMemoryString(ptr, 48, 32).startsWith("attack")));
        }

        @Override
        public void update() {
            if (API.readLong(address + 112) != moduleIdTemp) {
                update(address); // module type can change on the fly
            }

            super.update();
            info.update();
            health.update();
            target.update();

            if (isMoving()) {
                area.set(locationInfo.now, AVOID_RADIUS);
            }
        }

        @Override
        public String getModuleId() {
            return moduleId;
        }

        @Override
        public Type getType() {
            return moduleType;
        }

        @Override
        public void added(Main main) {
            super.added(main);
            target.added(main);
        }

        @Override
        public boolean use() {
            boolean allowEnemy = main.hero.invisible && main.config.GENERAL.ROAMING.ENEMY_CBS_INVISIBLE;
            return !allowEnemy && info.isEnemy() && isDangerousModule();
        }

        private boolean isDangerousModule() {
            return moduleType == Type.LASER_HR || moduleType == Type.LASER_MR || moduleType == Type.LASER_LR
                    || moduleType == Type.ROCKET_LA || moduleType == Type.ROCKET_MA;
        }

        @Override
        public @Nullable eu.darkbot.api.game.entities.Entity getTarget() {
            return target.targetedEntity;
        }

        @Override
        public boolean isAttacking() {
            return target.laserAttacking;
        }

        @Override
        public String toString() {
            return moduleType.toString();
        }
    }
}
