package eu.darkbot.api.managers;

import eu.darkbot.api.API;

import java.util.List;

/**
 * API for special event: Eternal Gate
 */
public interface EternalGateAPI extends API {
    int getKeys();
    int getBoosterPoints();
    int getCurrentWave();
    int getFurthestWave();

    List<Booster> getActiveBoosters();
    List<Booster> getBoosterOptions();

    /**
     * Booster for special event: eternal gate
     */
    interface Booster {
        int getPercentage();
        String getCategory();
    }
}
