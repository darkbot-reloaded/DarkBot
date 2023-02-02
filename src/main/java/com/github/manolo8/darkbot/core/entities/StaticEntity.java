package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.Main;

import java.util.Locale;

public class StaticEntity extends Entity implements eu.darkbot.api.game.entities.StaticEntity {

    public StaticEntity(int id, long address) {
        super(id, address);
    }

    public static class PodHeal extends StaticEntity implements eu.darkbot.api.game.entities.StaticEntity.PodHeal {
        public PodHeal(int id, long address) {
            super(id, address);
        }
    }

    public static class BuffCapsule extends StaticEntity implements eu.darkbot.api.game.entities.StaticEntity.BuffCapsule {
        public BuffCapsule(int id, long address) {
            super(id, address);
        }
    }

    public static class BurningTrail extends StaticEntity implements eu.darkbot.api.game.entities.StaticEntity.BurningTrail {
        public BurningTrail(int id, long address) {
            super(id, address);
        }

        @Override
        public boolean isOwn() {
            return true;
        }

        @Override
        public String toString() {
            return super.toString() + ", isOwned: " + isOwn();
        }
    }

    public static class BurningTrailEnemy extends BurningTrail {

        public BurningTrailEnemy(int id, long address) {
            super(id, address);
        }

        @Override
        public boolean isOwn() {
            return false;
        }
    }

    public static class PlutusGenerator extends StaticEntity implements eu.darkbot.api.game.entities.StaticEntity.PlutusGenerator {
        public PlutusGenerator(int id, long address) {
            super(id, address);
        }

        @Override
        public boolean isHealType() {
            return false;
        }

        @Override
        public String toString() {
            return super.toString() + ", isHealType: " + isHealType();
        }
    }

    public static class PlutusGeneratorGreen extends PlutusGenerator {
        public PlutusGeneratorGreen(int id, long address) {
            super(id, address);
        }

        @Override
        public boolean isHealType() {
            return true;
        }
    }

    public static class PetBeacon extends StaticEntity implements eu.darkbot.api.game.entities.StaticEntity.PetBeacon {
        private Type type;

        public PetBeacon(int id, long address) {
            super(id, address);
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public void update(long address) {
            super.update(address);

            String val = Main.API.readString(address, 120, 56, 40).toUpperCase(Locale.ROOT);
            type = Type.valueOf(val);
        }

        @Override
        public String toString() {
            return super.toString() + ", type: " + getType();
        }
    }
}
