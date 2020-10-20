package eu.darkbot.api.entities;

import eu.darkbot.api.objects.Health;
import eu.darkbot.api.objects.LocationInfo;
import org.jetbrains.annotations.Nullable;

public interface Ship extends Entity, Health {

    boolean isInvisible();
    boolean isEnemy();
    boolean isAiming(Ship other);
    boolean isAttacking(Ship other);

    String getUsername();

    boolean hasPet();
    Pet getPet();

    int getClanId();
    int getClanDiplomacy();
    String getClanTag();

    /**
     * Returns ship faction as int.
     * 1 = MMO, 2 = EIC, 3 = VRU,
     * 0 = probably npc
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
}
