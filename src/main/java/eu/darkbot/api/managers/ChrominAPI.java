package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.objects.Gui;

/**
 * API for chromin data
 */
public interface ChrominAPI extends API {
    double getCurrentAmount();
    double getMaxAmount();
}
