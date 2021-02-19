package eu.darkbot.api.entities.other;

/**
 * Represents ammunition in-game
 */
public interface Ammo {

    enum Laser {
        UNKNOWN,
        LCB_10,
        MCB_25,
        MCB_50,
        UCB_100,
        SAB_50,
        CBO_100,
        RSB_75(true),
        JOB_100,
        RB_214,
        PIB_100,
        SPACECUP_GROUP_A,
        SPACECUP_GROUP_B,
        SPACECUP_GROUP_C,
        SPACECUP_GROUP_D,
        SPACECUP_GROUP_E,
        SPACECUP_GROUP_F,
        SPACECUP_GROUP_G,
        SPACECUP_GROUP_H,
        A_BL,
        RCB_140,
        IDB_125,
        VB_142,
        EMAA_20,
        SBL_100;

        private final boolean cooldown;

        Laser() {
            this(false);
        }

        Laser(boolean cooldown) {
            this.cooldown = cooldown;
        }

        public String getId() {
            return "ammunition_laser_" + name().toLowerCase().replace("_", "-");
        }

        public boolean hasCooldown() {
            return cooldown;
        }

        public static Laser of(String id) {
            for (Laser laser : values())
                if (id.endsWith(laser.name().replaceAll("_", "-").toLowerCase()))
                    return laser;

            return null;
        }
    }

    enum Rocket {
        R_310,
        PLT_2026,
        PLT_2021,
        PLT_3030,
        PLD_8(true),
        DCR_250(true),
        WIZ_X(true),
        BDR_1211,
        R_IC3(true),
        SR_5(true),
        K_300M(true),
        SP_100X(true),
        AGT_500;

        private final boolean isSpecial; // cooldown of this rocket is much longer than regular one.

        Rocket() {
            this(false);
        }

        Rocket(boolean isSpecial) {
            this.isSpecial = isSpecial;
        }

        public String getId() {
            return "ammunition_" + (isSpecial ? "specialammo_" : "rocket_") +
                    name().toLowerCase().replace("_", "-");
        }
    }

    enum Mine {
        ACM_01,
        EMPM_01,
        SABM_01,
        DDM_01,
        SLM_01,
        IM_01,
        AGL_M01,
    }
}
