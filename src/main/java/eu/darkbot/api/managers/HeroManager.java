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

    boolean hasTarget();
    Ship getTarget();
    void setTarget(Ship target);

    default long timeTo(double distance) {
        return (long) (distance * 1000 / getSpeed());
    }

    default long timeTo(Location destination) {
        return timeTo(getLocationInfo().distance(destination));
    }

    default long timeTo(LocationInfo destination) {
        return timeTo(getLocationInfo().distance(destination));
    }

    default long timeTo(Entity destination) {
        return timeTo(getLocationInfo().distance(destination));
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
}
