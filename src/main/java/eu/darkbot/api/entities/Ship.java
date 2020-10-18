package eu.darkbot.api.entities;

import eu.darkbot.api.objects.Health;
import eu.darkbot.api.objects.LocationInfo;

public interface Ship extends Entity, Health {
    boolean isInvisible();
    boolean isEnemy();
    boolean isAiming(Ship other);
    boolean isAttacking(Ship other);

    String getUsername();
    String getClanTag();

    int getFactionId();
    int getRank();
    int getClanId();
    int getClanDiplomacy();

    /**
     * whats that?
     */
    int getGG();

    int getSpeed();
    double getAngle();
    long getTargetAddress();
    LocationInfo getDestination();
}
