package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.Npc;
import eu.darkbot.api.entities.Ship;
import eu.darkbot.api.entities.other.Formation;
import org.jetbrains.annotations.Nullable;

/**
 * This {@link API} represent hero entity,
 * from here you can manage your ship.
 */
public interface HeroAPI extends Ship, API.Singleton {

    /**
     * @return current used {@link Configuration}
     * @see #toggleConfiguration()
     */
    Configuration getConfiguration();

    /**
     * Toggles in-game {@link Configuration}
     *
     * @see #getConfiguration()
     */
    void toggleConfiguration();

    /**
     *
     * @param formation
     */
    void setFormation(Formation formation);

    /**
     * Will check if {@link HeroAPI} is in given {@code mode}.
     *
     * @param configuration to check
     * @param formation to check
     * @return true if {@link HeroAPI} is in given config & formation
     */
    boolean isInMode(Configuration configuration, Formation formation);

    /**
     * Will check if {@link HeroAPI} is in given {@code mode},
     * if not will try to set {@code mode}
     *
     * @param configuration to set
     * @param formation to set
     * @return true if is in given mode
     */
    boolean setMode(Configuration configuration, Formation formation);

    /**
     * Will try to set predefined by user attack mode based on given {@code target}
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
     * Represents in-game {@link HeroAPI} configs.
     */
    enum Configuration {
        UNKNOWN,
        FIRST,
        SECOND;

        public static Configuration of(int config) {
            return config == 1 ? FIRST :
                    config == 2 ? SECOND : UNKNOWN;
        }
    }
}
