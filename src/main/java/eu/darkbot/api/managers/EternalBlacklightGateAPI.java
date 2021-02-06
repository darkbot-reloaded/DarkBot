package eu.darkbot.api.managers;

import eu.darkbot.api.API;

import java.util.List;

/**
 * API for the eternal blacklight galaxy gate
 */
public interface EternalBlacklightGateAPI extends API.Singleton {
    int getCpuCount();
    int getBoosterPoints();
    int getCurrentWave();
    int getFurthestWave();

    List<? extends Booster> getActiveBoosters();
    List<? extends Booster> getBoosterOptions();

    /**
     * Booster for eternal blacklight
     */
    interface Booster {
        int getPercentage();
        String getCategory();
    }
}
