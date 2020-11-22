package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.Npc;
import eu.darkbot.api.entities.Ship;
import eu.darkbot.config.ShipConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * From here you can manage your ship.
 * Where to move, whats your target etc.
 * TODO more docs
 */
public interface HeroAPI extends Ship, API {

    /*boolean hasTarget();

    Optional<Ship> getTarget();

    void setTarget(Ship target);*/

    boolean isInMode(int configuration, Character formation);

    default boolean isInMode(@NotNull ShipConfig shipConfig) {
        return isInMode(shipConfig.getConfiguration(), shipConfig.getFormation());
    }

    boolean setMode(int configuration, Character formation);

    default boolean setMode(@NotNull ShipConfig shipConfig) {
        return setMode(shipConfig.getConfiguration(), shipConfig.getFormation());
    }

    boolean setAttackMode(Npc target);
    boolean setAttackMode();
    boolean setRoamMode();
    boolean setRunMode();
}
