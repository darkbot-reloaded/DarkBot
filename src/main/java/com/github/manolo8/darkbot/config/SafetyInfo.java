package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.utils.Ignorable;
import com.github.manolo8.darkbot.core.entities.BattleStation;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.bases.BaseHeadquarters;
import com.github.manolo8.darkbot.core.entities.bases.BaseSpot;
import com.github.manolo8.darkbot.core.entities.bases.BaseStation;
import com.github.manolo8.darkbot.utils.I18n;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class SafetyInfo implements Serializable, Ignorable, eu.darkbot.api.config.types.SafetyInfo {
    public enum Type {
        PORTAL, CBS, BASE;
        public static Type of(Entity entity) {
            if (entity instanceof Portal) return PORTAL;
            if (entity instanceof BattleStation && // FIXME: split BattleStation classes
                    ((BattleStation) entity).hullId >= 0 && ((BattleStation) entity).hullId < 255) return CBS;
            if (entity instanceof BaseSpot) return BASE;
            return null;
        }
        public String toString() {
            return I18n.getOrDefault("safety_places.type." + name().toLowerCase(Locale.ROOT), name());
        }
    }

    public Type type;
    public int x, y;
    public transient Entity entity;
    public transient double distance;

    private transient final RunMode defaultRunMode; // Used to determine if it should be stored
    public RunMode runMode = RunMode.ALWAYS;
    public JumpMode jumpMode;
    public CbsMode cbsMode;

    public SafetyInfo() {
        this.defaultRunMode = null;
    }

    public SafetyInfo(Type type, int x, int y, Entity entity) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.entity = entity;
        this.runMode = this.defaultRunMode = getDefaultRunMode();
        if (type == Type.PORTAL) jumpMode = JumpMode.ESCAPING;
        if (type == Type.CBS) cbsMode = CbsMode.ALLY;
    }

    private RunMode getDefaultRunMode() {
        return (type == Type.PORTAL && ((Portal) entity).target != null && !((Portal) entity).target.gg) ||
                type == Type.BASE && (entity instanceof BaseHeadquarters || entity instanceof BaseStation) ?
                RunMode.ALWAYS : RunMode.NEVER;
    }

    @Override
    public boolean ignore() {
        return (x == 0 && y == 0) ||
                runMode == defaultRunMode &&
                jumpMode == (type == Type.PORTAL ? JumpMode.ESCAPING : null) &&
                cbsMode == (type == Type.CBS ? CbsMode.ALLY : null);
    }

    @Override
    public boolean writeAsNull() {
        return false;
    }

    // Running reasons this safety can be selected
    public enum RunMode {
        ALWAYS, ENEMY_FLEE_ONLY, REPAIR_ONLY, REFRESH, NEVER;
        public String toString() {
            return I18n.getOrDefault("safety_places.run_mode." + name().toLowerCase(Locale.ROOT), name());
        }
    }

    // PORTAL
    // Condition to jump
    public enum JumpMode {
        NEVER, ESCAPING, FLEEING, REPAIRING, ALWAYS, ALWAYS_OTHER_SIDE;
        public String toString() {
            return I18n.getOrDefault("safety_places.jump_mode." + name().toLowerCase(Locale.ROOT), name());
        }
    }

    // CBS
    // Condition to run to CBS
    public enum CbsMode {
        ALLY, ALLY_NEUTRAL;
        public String toString() {
            return I18n.getOrDefault("safety_places.cbs_mode." + name().toLowerCase(Locale.ROOT), name());
        }
    }

    public int diameter() {
        return type != Type.BASE ? 500 :
                entity instanceof BaseHeadquarters ? 2500 :
                        entity instanceof BaseStation ? 3000 : 500;
    }

    public int radius() {
        return diameter() / 2;
    }

    @Override
    public String toString() {
        String result = type.toString();
        if (type == Type.PORTAL && entity != null && ((Portal) entity).target != null)
            result += "(" + ((Portal) entity).target.name + ")";
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SafetyInfo that = (SafetyInfo) o;
        return x == that.x &&
                y == that.y &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, x, y);
    }

    @Override
    public eu.darkbot.api.config.types.SafetyInfo.Type getType() {
        return eu.darkbot.api.config.types.SafetyInfo.Type.values()[type.ordinal()];
    }

    @Override
    public eu.darkbot.api.config.types.SafetyInfo.RunMode getRunMode() {
        return eu.darkbot.api.config.types.SafetyInfo.RunMode.values()[runMode.ordinal()];
    }

    @Override
    public void setRunMode(eu.darkbot.api.config.types.SafetyInfo.RunMode runMode) {
        this.runMode = RunMode.values()[runMode.ordinal()];
    }

    @Override
    public eu.darkbot.api.config.types.SafetyInfo.JumpMode getJumpMode() {
        if (jumpMode == null) return null;
        return eu.darkbot.api.config.types.SafetyInfo.JumpMode.values()[jumpMode.ordinal()];
    }

    @Override
    public eu.darkbot.api.config.types.SafetyInfo.CbsMode getCbsMode() {
        if (cbsMode == null) return null;
        return eu.darkbot.api.config.types.SafetyInfo.CbsMode.values()[cbsMode.ordinal()];
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public int getDiameter() {
        return diameter();
    }

    @Override
    public Optional<eu.darkbot.api.game.entities.Entity> getEntity() {
        return Optional.ofNullable(entity);
    }

    @Override
    public void setDistance(double d) {
        this.distance = d;
    }

    @Override
    public double getDistance() {
        return this.distance;
    }
}
