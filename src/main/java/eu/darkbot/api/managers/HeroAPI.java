package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.Npc;
import eu.darkbot.api.entities.Ship;
import eu.darkbot.api.items.SelectableItem;
import eu.darkbot.shared.managers.HeroModeImpl;
import org.jetbrains.annotations.Nullable;

/**
 * This {@link API} represent hero entity,
 * from here you can manage your ship.
 */
public interface HeroAPI extends Ship, API.Singleton {

    /**
     * @return current used {@link Configuration}
     */
    Configuration getConfiguration();

    /**
     * @return currently used {@link Configuration}
     */
    SelectableItem.Formation getFormation();

    /**
     * Will check if {@link HeroAPI} is in given {@code mode}.
     *
     * A mode is the combination of a configuration and a formation
     *
     * @param mode the mode to check
     * @return true if {@link HeroAPI} is in given {@link Mode}
     */
    boolean isInMode(Mode mode);

    /**
     * Will check if {@link HeroAPI} is in the given {@link Mode},
     * if it isn't, it will try to set the {@link Mode}
     *
     * Keep in mind because of in-game cool-downs it can take a while
     * to apply the mode, you should keep on calling the function each
     * tick with the mode you want to keep set on your ship.
     *
     * Checking {@link #isInMode} beforehand is unadvised, simply call this directly.
     *
     * Unless you have user-defined modes in the config for your feature,
     * you'll probably find more use in one of the base modes:
     * @see #setAttackMode(Npc)
     * @see #setRoamMode()
     * @see #setRunMode()
     *
     * @param mode the flying mode to set
     * @return true if the ship is now flying in the given mode, false otherwise
     */
    boolean setMode(Mode mode);

    /**
     * Attempts to {@link #setMode} with the user-defined mode to attack this type of NPC.
     *
     * If no npc is selected you can use null for default attack configuration, however,
     * always prefer passing in the NPC for better user control over formations.
     *
     * @param target what Npc to configure attacking mode for
     * @return true if the ship is now flying in attack mode for this npc, false otherwise
     */
    boolean setAttackMode(@Nullable Npc target);

    /**
     * Attempts to {@link #setMode} with the user-defined mode to roam.
     * @return true if the ship is now flying in run mode, false otherwise
     */
    boolean setRoamMode();

    /**
     * Attempts to {@link #setMode} with the user-defined mode to run.
     * @return true if the ship is now flying in run mode, false otherwise
     */
    boolean setRunMode();

    /**
     * Represent a config mode, that the ship can run in
     * This is the combination of an in-game config and formation.
     *
     * In the future this will ideally support changing configuration & formation
     * but currently it is expected that the results are immutable and nonchanging.
     */
    interface Mode {

        static Mode of(Configuration configuration, SelectableItem.Formation formation) {
            return new HeroModeImpl(configuration, formation);
        }

        Configuration getConfiguration();
        SelectableItem.Formation getFormation();
    }

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
