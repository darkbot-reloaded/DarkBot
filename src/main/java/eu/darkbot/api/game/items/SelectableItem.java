package eu.darkbot.api.game.items;

import eu.darkbot.api.game.enums.PetGear;
import eu.darkbot.api.managers.HeroItemsAPI;

import java.util.Locale;

/**
 * Represents a type of in-game item, that can be selected via the hot bar or category bar
 *
 * @see Item
 * @see HeroItemsAPI
 */
public interface SelectableItem {

    /**
     * @return The in-game id of this item
     */
    String getId();

    /**
     * @return The category inside category bar that this item is found in
     */
    ItemCategory getCategory();

    /**
     * In-game laser ammo items that can be shot
     */
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
        SPACECUP_GROUP_A("ammunition_laser_spacecup_group-a"),
        SPACECUP_GROUP_B("ammunition_laser_spacecup_group-b"),
        SPACECUP_GROUP_C("ammunition_laser_spacecup_group-c"),
        SPACECUP_GROUP_D("ammunition_laser_spacecup_group-d"),
        SPACECUP_GROUP_E("ammunition_laser_spacecup_group-e"),
        SPACECUP_GROUP_F("ammunition_laser_spacecup_group-f"),
        SPACECUP_GROUP_G("ammunition_laser_spacecup_group-g"),
        SPACECUP_GROUP_H("ammunition_laser_spacecup_group-h"),
        A_BL,
        RCB_140(true),
        IDB_125,
        VB_142,
        EMAA_20,
        SBL_100;

        private static final String PREFIX = "ammunition_laser_";
        private final String id;
        private final boolean cooldown;

        Laser() {
            this(false);
        }

        Laser(boolean cooldown) {
            this(cooldown, null);
        }

        Laser(String id) {
            this(false, id);
        }

        Laser(boolean cooldown, String id) {
            if (id == null) id = PREFIX + name().toLowerCase(Locale.ROOT).replace('_', '-');
            this.id = id;
            this.cooldown = cooldown;
        }

        public static Laser of(String laserId) {
            for (Laser laser : values())
                if (laser.getId().equals(laserId))
                    return laser;
            return null;
        }

        /**
         * @return If this type of laser has a burst followed by a cooldown to its use
         */
        public boolean hasCooldown() {
            return cooldown;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public ItemCategory getCategory() {
            return ItemCategory.LASERS;
        }
    }

    /**
     * In-game rocket items that are fired regularly
     */
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

        private static final String PREFIX = "ammunition_rocket_",
                PREFIX_SPECIAL = "ammunition_specialammo_";
        private final String id;
        private final boolean isSpecial; // cooldown of this rocket is much longer than regular one.

        Rocket() {
            this(false);
        }

        Rocket(boolean isSpecial) {
            this.id = (isSpecial ? PREFIX_SPECIAL : PREFIX) + name().toLowerCase(Locale.ROOT).replaceAll("_", "-");
            this.isSpecial = isSpecial;
        }

        public static Rocket of(String rocketId) {
            for (Rocket rocket : values())
                if (rocket.getId().equals(rocketId))
                    return rocket;

            return null;
        }

        /**
         * @return If this rocket has a longer cooldown than normal
         */
        public boolean isSpecial() {
            return isSpecial;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public ItemCategory getCategory() {
            return ItemCategory.ROCKETS;
        }
    }

    /**
     * In-game rocket items that are fired using a rocket launcher
     */
    enum RocketLauncher implements SelectableItem {
        /**
         * The actual rocket launcher item, used to launch the rockets
         */
        HST_LAUNCHER("equipment_weapon_rocketlauncher_hst"),
        HSTRM_01,
        UBR_100,
        ECO_10,
        SAR_01,
        SAR_02,
        CBR,
        BDR1212,
        PIR_100;

        private static final String PREFIX = "ammunition_rocketlauncher_";
        private final String id;

        RocketLauncher() {
            this(null);
        }

        RocketLauncher(String id) {
            if (id == null) id = PREFIX + name().toLowerCase(Locale.ROOT).replaceAll("_", "-");
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public ItemCategory getCategory() {
            return ItemCategory.ROCKET_LAUNCHERS;
        }
    }

    /**
     * Special in-game items subclass this interface.
     * @see Special
     * @see Firework
     * @see Spray
     * @see Emote
     */
    interface SpecialItem extends SelectableItem {

        @Override
        default ItemCategory getCategory() {
            return ItemCategory.SPECIAL_ITEMS;
        }
    }

    /**
     * Represents special items in-game, like smart-bomb, insta-shield or EMP.
     */
    enum Special implements SpecialItem {
        SMB_01("ammunition_mine_smb-01"),
        ISH_01("equipment_extra_cpu_ish-01"),
        EMP_01("ammunition_specialammo_emp-01");

        private final String id;

        Special(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }
    }

    /**
     * In-game firework items, as well as the firework ignite action
     */
    enum Firework implements SpecialItem {
        FWX_S,
        FWX_M,
        FWX_L,
        FWX_COM,
        FWX_RZ,
        IGNITE;

        private static final String PREFIX = "ammunition_firework_";
        private final String id;

        Firework() {
            this.id = PREFIX + name().toLowerCase(Locale.ROOT).replaceAll("_", "-");
        }

        @Override
        public String getId() {
            return id;
        }
    }

    /**
     * In-game user-activated emote items
     */
    enum Emote implements SpecialItem {
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
        ALIEN_CHEERS,
        ALIEN_PARTY,
        ALIEN_FLYINGKISS,
        ALIEN_SMIRK,
        ALIEN_ROGER,
        PUMPKIN_SMILE,
        PUMPKIN_ALIEN,
        EAT_TURKEY,
        SNOWMAN_BRR,
        SNOWMAN_ROLLEYES,
        SNOWMAN_THINKING;

        private static final String PREFIX = "ammunition_specialammo_emote_";
        private final String id;

        Emote() {
            this.id = PREFIX + name().toLowerCase(Locale.ROOT).replaceAll("_", "-");
        }

        @Override
        public String getId() {
            return id;
        }
    }

    /**
     * In-game user-activated sprite items
     */
    enum Spray implements SpecialItem {
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
        CROWN,
        FIST_BUMP,
        HAND_SHAKE,
        BATMAN,
        HAND,
        TURKEYDINNER,
        FROZEN_HAND,
        SNOWFLAKE,
        YETI;

        private static final String PREFIX = "ammunition_specialammo_spray_";
        private final String id;

        Spray() {
            this.id = PREFIX + name().toLowerCase(Locale.ROOT).replaceAll("_", "-");
        }

        @Override
        public String getId() {
            return id;
        }
    }

    /**
     * In-game mine items that can be laid down on the map
     */
    enum Mine implements SelectableItem {
        ACM_01,
        EMPM_01,
        SABM_01,
        DDM_01,
        SLM_01,
        IM_01,
        AGL_M01;

        private static final String PREFIX = "ammunition_mine_";
        private final String id;

        Mine() {
            this.id = PREFIX + name().toLowerCase(Locale.ROOT).replaceAll("_", "-");
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public ItemCategory getCategory() {
            return ItemCategory.MINES;
        }
    }

    /**
     * Type of CPU item
     * @see Cpu
     */
    enum CpuType {
        CPU("equipment_extra_cpu_"),
        ROBOT("equipment_extra_robot_"),
        SPECIAL_AMMO("ammunition_specialammo_"),
        PORTAL("ammunition_ggportal_");
        String prefix;

        CpuType(String prefix) {
            this.prefix = prefix;
        }
    }

    /**
     * In-game CPU items that can be activated
     */
    enum Cpu implements SelectableItem {
        /** CPU increasing hit chance, costs {@link eu.darkbot.api.managers.OreAPI.Ore#XENOMIT} to run */
        AIM(CpuType.CPU),
        /** Automatically shoots rocket */
        AROL_X(CpuType.CPU),
        /** Cloack CPU to make ship invisible */
        CL04K(CpuType.CPU),
        /** Advanced jump to x-1 map */
        AJP_01(CpuType.CPU),
        /** Jump CPU to any map */
        JP(CpuType.CPU),
        /** Repair robot */
        REP(CpuType.ROBOT),
        /** Trade cpu to sell ores */
        HMD_07(CpuType.CPU),
        /** Automatic laser buyer */
        ALB_X(CpuType.CPU),
        /** Automatically shoots rocket launcher rockets */
        RLLB_X(CpuType.CPU),
        /** Automatic rocket buyer */
        RB_X(CpuType.CPU),
        /** Automatic fuel buyer for pet */
        FB_X(CpuType.CPU),
        /** Drone repair CPU */
        DR(CpuType.CPU),
        /** Antidote for infection status */
        ANTI_Z1(CpuType.CPU),
        /** Makes your ship less opaque for a short duration */
        GHOSTIFIER(CpuType.SPECIAL_AMMO),
        /** Spawns a Super Ice Meteoroid NPC */
        FROZEN_PORTAL(CpuType.SPECIAL_AMMO),
        /** Spawns a cube near you */
        CUBI_PORTAL(CpuType.SPECIAL_AMMO),
        /** Opens the Permafrost Fissure galaxy gate */
        PERMAFROST_FISSURE_CPU(CpuType.PORTAL),
        /** Opens the Quarantine Zone galaxy gate */
        QUARANTINE_ZONE_CPU(CpuType.PORTAL),
        /** Opens the Eternal Blacklight galaxy gate */
        ETERNAL_BLACKLIGHT_CPU(CpuType.PORTAL),
        /** Spawns a cube near you */
        ALIEN_CPU_CUBIKON(CpuType.SPECIAL_AMMO),
        /** Spawns a common NPC near you */
        ALIEN_CPU_COMMON(CpuType.SPECIAL_AMMO),
        /** Spawns a rare NPC near you */
        ALIEN_CPU_RARE(CpuType.SPECIAL_AMMO),
        /** Spawns a legendary NPC near you */
        ALIEN_CPU_LEGEND(CpuType.SPECIAL_AMMO),
        /** Saturn Ship spawner, pve exclusive */
        SS_CPU(CpuType.SPECIAL_AMMO),
        /** PVP immunity CPU, pve exclusive */
        PA_X(CpuType.CPU);

        private final String id;

        Cpu(CpuType type) {
            this.id = type.prefix + name().toLowerCase(Locale.ROOT).replaceAll("_", "-");
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public ItemCategory getCategory() {
            return ItemCategory.CPUS;
        }
    }

    /**
     * In-game auto-buy actions
     */
    enum AutoBuy implements SelectableItem {
        LCB_10,
        MCB_25,
        MCB_50,
        SAB_50,
        R_310(true),
        PLT_2026(true),
        PLT_2021(true),
        PLT_3030(true);

        private static final String PREFIX_LASER = "buy_ammunition_laser_",
                PREFIX_ROCKET = "buy_ammunition_rocket_";
        private final String id;

        AutoBuy() {
            this(false);
        }

        AutoBuy(boolean isRocket) {
            this.id = (isRocket ? PREFIX_ROCKET : PREFIX_LASER)
                    + name().toLowerCase(Locale.ROOT).replaceAll("_", "-");
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public ItemCategory getCategory() {
            return ItemCategory.BUY_NOW;
        }
    }

    /**
     * In-game nano-factory tech items
     */
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

        private static final String PREFIX = "tech_";
        private final String id;

        Tech() {
            this.id = PREFIX + name().toLowerCase(Locale.ROOT).replaceAll("_", "-");
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public ItemCategory getCategory() {
            return ItemCategory.TECH_ITEMS;
        }
    }

    /**
     * In-game ship abilities, what ships can use each will differ.
     */
    enum Ability implements SelectableItem {
        SPEARHEAD_ULTIMATE_CLOAK,
        SPEARHEAD_JAM_X,
        SPEARHEAD_TARGET_MARKER,
        SPEARHEAD_DOUBLE_MINIMAP,
        AEGIS_HP_REPAIR,
        AEGIS_SHIELD_REPAIR,
        AEGIS_REPAIR_POD,
        CITADEL_DRAW_FIRE,
        CITADEL_TRAVEL,
        CITADEL_PROTECTION,
        CITADEL_FORTIFY,
        SOLACE,
        SPECTRUM,
        VENOM,
        SENTINEL,
        DIMINISHER,
        LIGHTNING,
        ADMIN_ULTIMATE_CLOAKING("ability_admin-ultimate-cloaking"),
        TARTARUS_SPEED_BOOST,
        TARTARUS_RAPID_FIRE,
        MIMESIS_HOLOGRAM,
        MIMESIS_SCRAMBLE,
        MIMESIS_PHASE_OUT,
        GOLIATH_X_FROZEN_CLAW("ability_goliath-x_frozen-claw"),
        HECATE_PARTICLE_BEAM,
        DISRUPTOR_REDIRECT,
        DISRUPTOR_SHIELD_DISARRAY,
        DISRUPTOR_DDOL,
        BERSERKER_SHL,
        BERSERKER_BSK,
        BERSERKER_RVG,
        ZEPHYR_MMT,
        ZEPHYR_TBR,
        SOLARIS_INC,
        KERES_SPR,
        KERES_SLE,
        RETIARUS_SPC,
        RETIARUS_CHS,
        ORCUS_ASSIMILATE,
        HOLO_SELF_REVERSAL,
        HOLO_ENEMY_REVERSAL;


        private static final String PREFIX = "ability_";
        private final String id;

        Ability() {
            if (!name().contains("_")) this.id = PREFIX + name().toLowerCase(Locale.ROOT);
            else {
                String[] parts = name().toLowerCase(Locale.ROOT).split("_", 2);
                this.id = PREFIX + parts[0] + "_" + parts[1].replaceAll("_", "-");
            }
        }

        Ability(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public ItemCategory getCategory() {
            return ItemCategory.SHIP_ABILITIES;
        }
    }

    /**
     * In-game drone formations
     */
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

        private static final String PREFIX = "drone_formation_";
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
            this.id = PREFIX + id;
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
                if (formation.getId().equals(id))
                    return formation;

            return null;
        }

        /**
         * @return Shield gained or lost when using the formation, in percentage.
         */
        public double getShieldMultiplier() {
            return sh;
        }

        /**
         * @return Health gained or lost when using the formation, in percentage.
         */
        public double getHealthMultiplier() {
            return hp;
        }

        /**
         * @return Shield regeneration per second in percentage. 5% -> 0.005
         */
        public double getShieldRegen() {
            return sps;
        }


        @Override
        public String getId() {
            return id;
        }

        @Override
        public ItemCategory getCategory() {
            return ItemCategory.DRONE_FORMATIONS;
        }
    }

    /**
     * In-game hot bar selectable {@link PetGear}.
     * Note that not all gears are available here to use as items.
     */
    enum Pet implements SelectableItem {
        /** Enables {@link PetGear#MEGA_MINE} gear */
        G_MM1(PetGear.MEGA_MINE),
        /** Enables {@link PetGear#KAMIKAZE} gear */
        G_KK1(PetGear.KAMIKAZE),
        /** Enables {@link PetGear#SACRIFICIAL} gear */
        G_FS1(PetGear.SACRIFICIAL),
        /** Enables {@link PetGear#HP_LINK} gear */
        G_HPL1(PetGear.HP_LINK),
        /** Enables {@link PetGear#PET_TARGET} gear */
        G_RT1(PetGear.PET_TARGET),
        /** Enables {@link PetGear#BEACON_HP} gear */
        G_BH1(PetGear.BEACON_HP),
        /** Enables {@link PetGear#BEACON_COMBAT} gear */
        G_BC1(PetGear.BEACON_COMBAT);

        private static final String PREFIX = "equipment_petgear_";
        private final String id;
        private final PetGear petGear;

        Pet(PetGear petGear) {
            this.id = PREFIX + name().toLowerCase(Locale.ROOT).replaceAll("_", "-");
            this.petGear = petGear;
        }

        /**
         * @return The pet gear this item represents
         */
        public PetGear getGear() {
            return petGear;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public ItemCategory getCategory() {
            return ItemCategory.PET;
        }
    }
}
