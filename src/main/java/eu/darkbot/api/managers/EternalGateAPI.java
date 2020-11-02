package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.objects.EternalGateBooster;
import eu.darkbot.api.objects.Gui;

import java.util.List;

/**
 * API for special event: Eternal Gate
 */
public interface EternalGateAPI extends Gui, API {
    int getKeys();
    int getBoosterPoints();
    int getCurrentWave();
    int getFurthestWave();

    List<EternalGateBooster> getActiveBoosters();
    List<EternalGateBooster> getBoosterOptions();
}
