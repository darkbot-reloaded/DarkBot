package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO: 07.11.2020 better name sadge
public interface AttackAPI extends API {

    @Nullable
    Entity getTarget();
    boolean setTarget(@Nullable Entity entity);

    boolean isTargeted();
    boolean clickTarget();

    boolean isAttacking();
    boolean laserAttack();
    boolean laserAbort();

    boolean canBeClicked(@NotNull LaserAmmo ammo); //is given ammo in slotbar and can be handled via keyboard.
    LaserAmmo setLaserAmmo(@NotNull String laserId);
    LaserAmmo setLaserAmmo(@NotNull AttackAPI.LaserAmmo laserAmmo);

    boolean canBeClicked(@NotNull RocketAmmo ammo);
    RocketAmmo setRocketAmmo(@NotNull String rocketId);
    RocketAmmo setRocketAmmo(@NotNull AttackAPI.RocketAmmo rocketAmmo);

    enum LaserAmmo {
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

        LaserAmmo() {
            this(false);
        }

        LaserAmmo(boolean cooldown) {
            this.cooldown = cooldown;
        }

        public String getId() {
            return "ammunition_laser_" + name().toLowerCase().replace("_", "-");
        }

        public boolean hasCooldown() {
            return cooldown;
        }
    }

    enum RocketAmmo {
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

        RocketAmmo() {
            this(false);
        }

        RocketAmmo(boolean isSpecial) {
            this.isSpecial = isSpecial;
        }

        public String getId() {
            return "ammunition_" + (isSpecial ? "specialammo_" : "rocket_") +
                    name().toLowerCase().replace("_", "-");
        }
    }
}
