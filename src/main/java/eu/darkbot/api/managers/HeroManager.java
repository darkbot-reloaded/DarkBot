package eu.darkbot.api.managers;

import eu.darkbot.api.entities.Entity;
import eu.darkbot.api.entities.Npc;
import eu.darkbot.api.entities.Portal;
import eu.darkbot.api.entities.Ship;
import eu.darkbot.api.objects.LocationInfo;
import eu.darkbot.config.ShipConfig;
import eu.darkbot.utils.Location;

/**
 * From here you can manage your ship.
 * Where to move, whats your target etc.
 * TODO more docs
 */
public interface HeroManager extends Ship {

    Drive getDrive();

    boolean hasTarget();
    Ship getTarget();
    void setTarget(Ship target);

    boolean jumpPortal(Portal portal);

    default long timeTo(double distance) {
        return (long) (distance * 1000 / getSpeed());
    }

    default long timeTo(LocationInfo location) {
        return timeTo(getLocationInfo().distance(location));
    }

    boolean isInMode(int configuration, Character formation);

    default boolean isInMode(ShipConfig shipConfig) {
        return isInMode(shipConfig.CONFIG, shipConfig.FORMATION);
    }

    boolean setMode(int configuration, Character formation);

    default boolean setMode(ShipConfig shipConfig) {
        return setMode(shipConfig.CONFIG, shipConfig.FORMATION);
    }

    boolean setAttackMode(Npc target);
    boolean setAttackMode();
    boolean setRoamMode();
    boolean setRunMode();

    //should be added into heroManager directly?
    interface Drive {
        void stop(boolean currentLocation);

        void moveTo(double x, double y);

        default void moveTo(Location target) {
            moveTo(target.x, target.y);
        }

        default void moveTo(LocationInfo target) {
            moveTo(target.getLocation());
        }

        default void moveto(Entity target) {
            moveTo(target.getLocationInfo());
        }

        void moveRandom();
        boolean canMove();
        // hero thing should be overridden
        boolean isMoving();
        boolean isOutOfMap();
        Location movingTo();
    }
}
