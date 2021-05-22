package eu.darkbot.api.managers;

import eu.darkbot.api.API;

import java.util.List;

/**
 * API providing data for the eternal gate event in-game
 *
 * This API is very similar to the one provided in {@link EternalBlacklightGateAPI},
 * the main difference being the gate itself.
 *
 * For documentation as to what the methods mean
 * @see EternalBlacklightGateAPI
 */
public interface EternalGateAPI extends API.Singleton {
    int getKeys();
    int getBoosterPoints();
    int getCurrentWave();
    int getFurthestWave();

    List<? extends Booster> getActiveBoosters();
    List<? extends Booster> getBoosterOptions();

    /**
     * Booster for special event: eternal gate
     */
    interface Booster {
        int getPercentage();
        String getCategory();
    }
}
