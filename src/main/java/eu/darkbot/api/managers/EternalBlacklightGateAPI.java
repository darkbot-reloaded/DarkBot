package eu.darkbot.api.managers;

import eu.darkbot.api.API;

import java.util.List;

/**
 * API providing data for the eternal blacklight galaxy gate in-game
 *
 * TODO: include methods to accept the boosters
 * TODO: add a generic interface for both eternal gate APIs extended by both
 */
public interface EternalBlacklightGateAPI extends API.Singleton {
    /**
     * Making the gate appear on the map requires the use of one CPU (acts as a key).
     *
     * @return Amount of CPUs available.
     */
    int getCpuCount();

    /**
     * Booster points can be spent on boosters.
     * @see #getActiveBoosters()
     * @see #getBoosterOptions()
     *
     * @return Amount of booster points available
     */
    int getBoosterPoints();

    /**
     * @return The current wave displayed in-game
     */
    int getCurrentWave();

    /**
     * @return The furthest achieved wave displayed in-game
     */
    int getFurthestWave();

    /**
     * The list of currently active boosters affecting the
     * ship if inside the eternal blacklight galaxy gate.
     *
     * @return The list of all currently active boosters
     */
    List<? extends Booster> getActiveBoosters();

    /**
     * The list of booster options to pick from, by spending booster points.
     * Once picked they will get added up to your active boosters.
     *
     * @see #getBoosterPoints()
     * @see #getActiveBoosters()
     *
     * @return The list of available options to exchange
     */
    List<? extends Booster> getBoosterOptions();

    /**
     * Booster for eternal black light
     */
    interface Booster {
        /**
         * @return Percentage of boost in plain number. 5% = 5
         */
        int getPercentage();

        /**
         * The category is an in-game id, usually fully capitalized with underscores.
         *
         * @return Booster category id
         */
        String getCategory();
    }
}
