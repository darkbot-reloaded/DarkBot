package eu.darkbot.api.managers;

import eu.darkbot.api.API;

/**
 * API for chromin data
 */
public interface ChrominAPI extends API.Singleton {
    double getCurrentAmount();
    double getMaxAmount();
}
