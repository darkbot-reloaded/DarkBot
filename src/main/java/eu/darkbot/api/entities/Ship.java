package eu.darkbot.api.entities;

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
     * Returns true if ship is considered as enemy for {@link eu.darkbot.api.managers.HeroManager}.
     * Is from other faction || is in enemy clan
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
        TURTLE,
        ARROW,
        LANCE,
        START,
        PINCER,
        DOUBLE_ARROW,
        DIAMOND,
        DOUBLE_CHEVRON,
        MOTH,
        CRAB,
        HEART,
        BARRAGE,
        BAT,
        // 3D formation
        RING,
        DRILL,
        VETERAN,
        DOME,
        WHEEL,
        X,
        WAVY,
        MOSQUITO
    }
}
