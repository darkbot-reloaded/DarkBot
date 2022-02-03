package com.github.manolo8.darkbot.core.entities;

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
    }

    public static class PlutusGenerator extends StaticEntity implements eu.darkbot.api.game.entities.StaticEntity.PlutusGenerator {
        public PlutusGenerator(int id, long address) {
            super(id, address);
        }
    }
}
