package eu.darkbot.api.entities.other;

import eu.darkbot.api.managers.HeroItemsAPI;
import eu.darkbot.utils.StringUtils;

public interface SelectableItem {

    boolean matches(String itemId);

    default HeroItemsAPI.Category getCategory() {
        return null;
    }

    enum Laser implements SelectableItem {
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
        RCB_140(true),
        IDB_125,
        VB_142,
        EMAA_20,
        SBL_100;

        private final String id;
        private final boolean cooldown;

        Laser() {
            this(false);
        }

        Laser(boolean cooldown) {
            this.id = "ammunition_laser_" +
                    StringUtils.replaceLastOccurrence(name().toLowerCase(), '_', '-');
            this.cooldown = cooldown;
        }

        public static Laser of(String laserId) {
            for (Laser laser : values())
                if (laser.matches(laserId))
                    return laser;

            return null;
        }

        public boolean hasCooldown() {
            return cooldown;
        }

        @Override
        public boolean matches(String itemId) {
            return itemId.endsWith(id);
        }

        @Override
        public HeroItemsAPI.Category getCategory() {
            return HeroItemsAPI.Category.LASERS;
        }
    }

    enum Rocket implements SelectableItem {
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

        private final String id;
        private final boolean isSpecial; // cooldown of this rocket is much longer than regular one.

        Rocket() {
            this(false);
        }

        Rocket(boolean isSpecial) {
            this.id = name().toLowerCase().replaceAll("_", "-");
            this.isSpecial = isSpecial;
        }

        public static Rocket of(String rocketId) {
            for (Rocket rocket : values())
                if (rocket.matches(rocketId))
                    return rocket;

            return null;
        }

        public boolean isSpecial() {
            return isSpecial;
        }

        @Override
        public boolean matches(String itemId) {
            return itemId.endsWith(id);
        }

        @Override
        public HeroItemsAPI.Category getCategory() {
            return HeroItemsAPI.Category.ROCKETS;
        }
    }

    enum RocketLauncher implements SelectableItem {
        HST,
        HSTRM_01,
        UBR_100,
        ECO_10,
        SAR_01,
        SAR_02,
        CBR,
        BDR1212,
        PIR_100;

        private final String id;

        RocketLauncher() {
            this.id = name().toLowerCase().replaceAll("_", "-");
        }

        @Override
        public boolean matches(String itemId) {
            return itemId.endsWith(id);
        }

        @Override
        public HeroItemsAPI.Category getCategory() {
            return HeroItemsAPI.Category.ROCKET_LAUNCHERS;
        }
    }

    //if someone want to can separate emotes & sprays
    enum Special implements SelectableItem {
        SMB_01,
        ISH_01,
        EMP_01,
        FWX_S,
        FWX_M,
        FWX_L,
        FWX_COM,
        FWX_RZ,
        IGNITE,
        ALIEN_ANGRY,
        ALIEN_CRY,
        ALIEN_HORROR,
        ALIEN_SMILE,
        ALIEN_WINK,
        BUNNY_CONFUSED,
        BUNNY_COOL,
        BUNNY_SAD,
        BUNNY_SLEEPY,
        BUNNY_STARE,
        ALIEN_LAUGHCRY,
        ALIEN_COOL,
        ALIEN_SORRY,
        ALIEN_MASK_UP,
        ALIEN_SICK,
        INJECTION,
        ALIEN_DIZZY,
        ALIEN_SLEEPY,
        ALIEN_STARRYEYES,
        CROSS,
        HEARTS,
        KO,
        LIKE,
        SKULL,
        EGGLE,
        SPACE_BUNNY,
        EGGTOPIA,
        EASTER_EGGS,
        BIOHAZARD,
        HEALING,
        WASH_HANDS,
        SHOOT,
        WATCHINGYOU,
        HEARTBREAK,
        STRONG,
        CELEBRATE,
        PEACE,
        QUESTION,
        ALIEN_CHEERS,
        ALIEN_PARTY,
        ALIEN_FLYINGKISS,
        ALIEN_SMIRK,
        ALIEN_ROGER,
        CROWN,
        FIST_BUMP,
        HAND_SHAKE,
        PUMPKIN_SMILE,
        PUMPKIN_ALIEN,
        BATMAN,
        HAND,
        TURKEYDINNER,
        EAT_TURKEY,
        SNOWMAN_BRR,
        SNOWMAN_ROLLEYES,
        SNOWMAN_THINKING,
        FROZEN_HAND,
        SNOWFLAKE,
        YETI;

        private final String id;

        Special() {
            this.id = name().toLowerCase().replaceAll("_", "-");
        }

        @Override
        public boolean matches(String itemId) {
            return itemId.endsWith(id);
        }

        @Override
        public HeroItemsAPI.Category getCategory() {
            return HeroItemsAPI.Category.SPECIAL_ITEMS;
        }

    }

    enum Mine implements SelectableItem {
        ACM_01,
        EMPM_01,
        SABM_01,
        DDM_01,
        SLM_01,
        IM_01,
        AGL_M01;

        private final String id;

        Mine() {
            this.id = name().toLowerCase().replaceAll("_", "-");
        }

        @Override
        public boolean matches(String itemId) {
            return itemId.endsWith(id);
        }

        @Override
        public HeroItemsAPI.Category getCategory() {
            return HeroItemsAPI.Category.MINES;
        }
    }

    enum Cpu implements SelectableItem {
        AIM,
        AROL_X,
        CL04K,
        AJP_01,
        JP,
        REP,
        HMD_07,
        ALB_X,
        RLLB_X,
        RB_X,
        FB_X,
        DR,
        ANTI_Z1,
        GHOSTIFIER,
        FROZEN_PORTAL,
        CUBI_PORTAL,
        PERMAFROST_FISSURE_CPU,
        QUARANTINE_ZONE_CPU,
        ETERNAL_BLACKLIGHT_CPU,
        ALIEN_CPU_CUBIKON,
        ALIEN_CPU_COMMON,
        ALIEN_CPU_RARE,
        ALIEN_CPU_LEGEND,
        SS_CPU,
        PA_X;

        private final String id;

        Cpu() {
            this.id = name().toLowerCase().replaceAll("_", "-");
        }

        @Override
        public boolean matches(String itemId) {
            return itemId.endsWith(id);
        }

        @Override
        public HeroItemsAPI.Category getCategory() {
            return HeroItemsAPI.Category.CPUS;
        }
    }

    enum Buy implements SelectableItem {
        LCB_10,
        MCB_25,
        MCB_50,
        SAB_50,
        R_310(true),
        PLT_2026(true),
        PLT_2021(true),
        PLT_3030(true);

        private final String id;

        Buy() {
            this(false);
        }

        Buy(boolean isRocket) {
            this.id = "buy_ammunition_" + (isRocket ? "rocket_" : "laser_")
                    + name().toLowerCase().replaceAll("_", "-");
        }

        @Override
        public boolean matches(String itemId) {
            return itemId.endsWith(id);
        }

        @Override
        public HeroItemsAPI.Category getCategory() {
            return HeroItemsAPI.Category.BUY_NOW;
        }
    }

    enum Tech implements SelectableItem {
        ENERGY_LEECH,
        CHAIN_IMPULSE,
        PRECISION_TARGETER,
        BACKUP_SHIELDS,
        BATTLE_REPAIR_BOT,
        BURNING_TRAIL,
        EXPLOSIVE_CHARGING_BLOB,
        NPC_NUKE,
        BATTLE_REPAIR_BOT2,
        SHIELD_BACKUP2,
        SPEED_BURST,
        ANTI_MINE_TRANSMITTER,
        ANTI_AI_EMERGENCY_RESERVES,
        FROST_FIELD;

        private final String id;

        Tech() {
            this.id = name().toLowerCase().replaceAll("_", "-");
        }

        @Override
        public boolean matches(String itemId) {
            return itemId.endsWith(id);
        }

        @Override
        public HeroItemsAPI.Category getCategory() {
            return HeroItemsAPI.Category.TECH_ITEMS;
        }
    }

    //probably would be better to use constant ids and custom enum names.
    enum Ability implements SelectableItem {
        ULTIMATE_CLOAK,
        JAM_X,
        TARGET_MARKER,
        DOUBLE_MINIMAP,
        HP_REPAIR,
        SHIELD_REPAIR,
        REPAIR_POD,
        DRAW_FIRE,
        TRAVEL,
        PROTECTION,
        FORTIFY,
        SOLACE,
        SPECTRUM,
        VENOM,
        SENTINEL,
        DIMINISHER,
        LIGHTNING,
        ADMIN_ULTIMATE_CLOAKING,
        SPEED_BOOST,
        RAPID_FIRE,
        HOLOGRAM,
        SCRAMBLE,
        PHASE_OUT,
        FROZEN_CLAW,
        PARTICLE_BEAM,
        REDIRECT,
        SHIELD_DISARRAY,
        DDOL,
        SHL,
        BSK,
        RVG,
        MMT,
        TBR,
        INC,
        SPR,
        SLE,
        SPC,
        CHS,
        ASSIMILATE;

        private final String id;

        Ability() {
            this.id = name().toLowerCase().replaceAll("_", "-");
        }

        @Override
        public boolean matches(String itemId) {
            return itemId.endsWith(id);
        }

        @Override
        public HeroItemsAPI.Category getCategory() {
            return HeroItemsAPI.Category.SHIP_ABILITIES;
        }
    }

    enum Formation implements SelectableItem {

        /**
         * 2D formations.
         */
        STANDARD("default"),
        TURTLE("f-01-tu", 0.1),
        ARROW("f-02-ar"),
        LANCE("f-03-la"),
        STAR("f-04-st"),
        PINCER("f-05-pi"),
        DOUBLE_ARROW("f-06-da", -0.2),
        DIAMOND("f-07-di", -0.3, 0, 0.01),
        CHEVRON("f-08-ch", -0.2, 0),
        MOTH("f-09-mo", 0.2, 0, -0.05),
        CRAB("f-10-cr"),
        HEART("f-11-he", 0.2, 0.2),
        BARRAGE("f-12-ba"),
        BAT("f-13-bt"),

        /**
         * 3D formations.
         */
        RING("f-3d-rg", 0.85),
        DRILL("f-3d-dr", -0.25),
        VETERAN("f-3d-vt", -0.2, -0.2),
        DOME("f-3d-dm", 0, 0.3, 0.005),
        WHEEL("f-3d-wl", 0, 0, -0.05),
        X("f-3d-x", 0.08, 0),
        WAVY("f-3d-wv"),
        MOSQUITO(null),

        /**
         * Not sure what is it but exists in-game source with ID 42.
         */
        X2(null);

        private final String id;
        private final double hp, sh, sps;

        Formation(String id) {
            this(id, 0);
        }

        Formation(String id, double sh) {
            this(id, 0, sh, 0);
        }

        Formation(String id, double hp, double sh) {
            this(id, hp, sh, 0);
        }

        Formation(String id, double hp, double sh, double sps) {
            this.id = id;
            this.hp = hp;
            this.sh = sh;
            this.sps = sps;
        }

        public static Formation of(int formationId) {
            if (formationId == 42) return X2;
            if (formationId < 0 || formationId >= values().length) return STANDARD;
            return values()[formationId];
        }

        public static Formation of(String id) {
            for (Formation formation : values())
                if (formation.matches(id))
                    return formation;

            return null;
        }

        public double getShieldMultiplier() {
            return sh;
        }

        public double getHealthMultiplier() {
            return hp;
        }

        /**
         * @return shield regen % amount per second.
         */
        public double getShieldRegen() {
            return sps;
        }


        @Override
        public boolean matches(String itemId) {
            return itemId.endsWith(id);
        }

        @Override
        public HeroItemsAPI.Category getCategory() {
            return HeroItemsAPI.Category.DRONE_FORMATIONS;
        }
    }

    enum Pet implements SelectableItem {
        G_MM1(Gear.MEGA_MINE),
        G_KK1(Gear.KAMIKAZE),
        G_FS1(null), //todo
        G_HPL1(Gear.HP_LINK),
        G_RT1(null),//todo
        G_BH1(Gear.BEACON_HP),
        G_BC1(Gear.BEACON_COMBAT);

        private final String id;
        private final Gear gear;

        Pet(Gear gear) {
            this.id = name().toLowerCase().replaceAll("_", "-");
            this.gear = gear;
        }

        public Gear getGear() {
            return gear;
        }

        @Override
        public boolean matches(String itemId) {
            return itemId.endsWith(id);
        }

        @Override
        public HeroItemsAPI.Category getCategory() {
            return HeroItemsAPI.Category.PET;
        }
    }
}
