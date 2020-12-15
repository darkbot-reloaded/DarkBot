package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.Npc;
import eu.darkbot.api.entities.Ship;
import eu.darkbot.config.ConfigAPI;
import org.jetbrains.annotations.NotNull;

/**
 * This {@link API} represent hero entity,
 * from here you can manage your ship.
 */
public interface HeroAPI extends Ship, API {

    int getConfiguration();

    void toggleConfiguration();

    void sendLaserAttack();

    void sendRocketAttack();

    boolean isLogout();

    void tryLogout();

    /**
     * Will check if {@link HeroAPI} is in given {@code mode}.
     *
     * @param mode to be checked
     * @return true if {@link Mode#getConfig()} and {@link Mode#getFormation()} equals current
     */
    boolean isInMode(@NotNull HeroAPI.Mode mode);

    boolean isInMode(int configuration, Character formation);

    default boolean isInMode(@NotNull ConfigAPI.ShipConfig shipConfig) {
        return isInMode(shipConfig.CONFIG, shipConfig.FORMATION);
    }

    /**
     * Will check if {@link HeroAPI} is in given {@code mode},
     * if not will try to set {@code mode}
     *
     * @param mode to be set
     * @return true if is in given mode
     */
    boolean setMode(@NotNull HeroAPI.Mode mode);

    boolean setMode(int configuration, Character formation);

    default boolean setMode(@NotNull ConfigAPI.ShipConfig shipConfig) {
        return setMode(shipConfig.CONFIG, shipConfig.FORMATION);
    }

    /**
     * Will try to set predefined by user attack {@link Mode} based on given {@code target}
     *
     * @param target to get predefined formation from
     * @return true if is in attack mode
     */
    boolean setAttackMode(Npc target);

    boolean setAttackMode();

    /**
     * @return true if is in roam mode
     */
    boolean setRoamMode();

    /**
     * @return true if is in run mode
     */
    boolean setRunMode();

    /**
     * Configuration mode.
     */
    interface Mode {

        // TODO
        static Mode of(int config, Ship.Formation formation) {
            //return new ShipConfig(config, formation);
            return null;
        }

        int getConfig();

        Ship.Formation getFormation();
    }
}
