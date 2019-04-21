package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.core.entities.BasePoint;
import com.github.manolo8.darkbot.core.entities.BattleStation;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.entities.Portal;

import java.util.Objects;

public class SafetyInfo {
    public enum Type {
        PORTAL("Port"), CBS("CBS"), BASE("Base");
        public static Type of(Entity entity) {
            if (entity instanceof Portal) return PORTAL;
            if (entity instanceof BattleStation) return CBS;
            if (entity instanceof BasePoint) return BASE;
            return null;
        }
        String text;
        Type(String text) {this.text = text;}
        public String toString() {return text;}
    }

    public Type type;
    public int x, y, diameter;
    public transient Entity entity;
    public transient double distance;

    public SafetyInfo() {}

    public SafetyInfo(Type type, int x, int y, Entity entity) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.diameter = type == Type.BASE ? 1500 : 500;
        this.runMode = type == Type.PORTAL && !((Portal) entity).target.gg ? RunMode.ALWAYS : RunMode.NEVER;
        if (type == Type.PORTAL) jumpMode = JumpMode.ESCAPING;
        if (type == Type.CBS) cbsMode = CbsMode.ALLY;
        this.entity = entity;
    }

    // Running reasons this safety can be selected
    public enum RunMode {
        ALWAYS("Always"),
        ENEMY_FLEE_ONLY("Fleeing (Enemy on sight)"),
        REPAIR_ONLY("Repairing (No enemies)"),
        NEVER("Never");
        String text;
        RunMode(String text) {this.text = text;}
        public String toString() {return text;}
    }
    public RunMode runMode = RunMode.ALWAYS;

    // PORTAL
    // Condition to jump
    public enum JumpMode {
        NEVER("Never"),
        ESCAPING("Escaping (Enemy shooting)"),
        FLEEING("Fleeing (Enemy on sight)"),
        ALWAYS("Always"),
        ALWAYS_OTHER_SIDE("Always (repair on other side)");
        String text;
        JumpMode(String text) {this.text = text;}
        public String toString() {return text;}
    }
    public JumpMode jumpMode;

    // CBS
    // Condition to run to CBS
    public enum CbsMode {
        ALLY("Ally CBS"), ALLY_NEUTRAL("Ally or empty");
        String text;
        CbsMode(String text) {this.text = text;}
        public String toString() {return text;}
    }
    public CbsMode cbsMode;

    public int radius() {
        return diameter / 2;
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
                diameter == that.diameter &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, x, y, diameter);
    }

}
