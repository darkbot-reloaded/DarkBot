package eu.darkbot.api.managers;

import eu.darkbot.api.API;

/**
 * API providing data for the chromin rush event in-game
 */
public interface ChrominAPI extends API.Singleton {

    /**
     * @return The current amount of chromin owned by the user
     */
    double getCurrentAmount();

    /**
     * @return The maximum amount of chromin allowed. Usually 160 000
     */
    double getMaxAmount();
}
