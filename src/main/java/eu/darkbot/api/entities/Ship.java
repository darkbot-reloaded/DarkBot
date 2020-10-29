package eu.darkbot.api.entities;

import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.objects.Health;
import eu.darkbot.api.objects.Locatable;
import eu.darkbot.api.objects.LocationInfo;
import org.jetbrains.annotations.Nullable;

public interface Ship extends Entity, Health {

    /**
     * @return true if ship is invisible/cloaked.
     */
    boolean isInvisible();

    /**
     * Returns true if ship is considered as enemy for {@link HeroAPI}.
     * Is not in ally clan. Group?
     * Is from other faction or enemy clan.
     */
    boolean isEnemy();

    /**
     * Returns true if ship aims other ship by checking theirs angle.
     */
    boolean isAiming(Ship other);

    // TODO: 28.10.2020 need proper doc
    boolean isAttacking(Ship other);

    /**
     * @return ship username.
     */
    String getUsername();

    /**
     * @return true if ship has enabled {@link Pet}.
     */
    boolean hasPet();
    Pet getPet();

    int getClanId();
    int getClanDiplomacy();
    String getClanTag();

    /**
     * Returns ship faction as int.
     * 0 = NONE, 1 = MMO, 2 = EIC, 3 = VRU, 4 = KRONOS?
     */
    int getFactionId();

    /**
     * Probably id of rank icon.
     */
    int getRankIconId();

    /**
     * Probably id of gate circles icon, above rank
     */
    int getGalaxyRankIconId();

    /**
     * @return speed of ship from memory.
     */
    int getSpeed();

    /**
     * Calculates needed time to travel given distance.
     * @return time in milliseconds needed to travel given distance
     */
    default long timeTo(double distance) {
        return (long) (distance * 1000 / getSpeed());
    }

    default long timeTo(Locatable destination) {
        return timeTo(getLocationInfo().distanceTo(destination));
    }

    /**
     * @return angle of ship from memory.
     */
    double getAngle();

    /**
     * @return address of current entity target
     */
    long getTargetAddress();

    /**
     * @return {@link LocationInfo} if has destination otherwise null.
     */
    @Nullable
    LocationInfo getDestination();

    /**
     * Returns true if ship has enabled given formation.
     */
    boolean isInFormation(int formationId);

    default boolean isInFormation(Ship.Formation formation) {
        return isInFormation(formation.ordinal());
    }

    enum Formation {
        // 2D formations
        STANDARD,
        TURTLE(0.1),
        ARROW,
        LANCE,
        STAR,
        PINCER,
        DOUBLE_ARROW(-0.2),
        DIAMOND(-0.3, 0, 0.01),
        CHEVRON(-0.2, 0),
        MOTH(0.2, 0, -0.05),
        CRAB,
        HEART(0.2, 0.2),
        BARRAGE,
        BAT,
        // 3D formations
        RING(0.85),
        DRILL(-0.25),
        VETERAN(-0.2, -0.2),
        DOME(0, 0.3, 0.005),
        WHEEL(0, 0, -0.05),
        X(0.08, 0),
        WAVY,
        MOSQUITO;

        private double hp, sh, sps;

        Formation() {
        }

        Formation(double sh) {
            this.sh = sh;
        }

        Formation(double hp, double sh) {
            this.hp = hp;
            this.sh = sh;
        }

        Formation(double hp, double sh, double sps) {
            this.hp = hp;
            this.sh = sh;
            this.sps = sps;
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
    }
}
