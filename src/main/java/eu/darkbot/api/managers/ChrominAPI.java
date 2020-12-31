package eu.darkbot.api.managers;

import eu.darkbot.api.API;

/**
 * API for chromin data
 */
public interface ChrominAPI extends API {
    double getCurrentAmount();
    double getMaxAmount();
}
