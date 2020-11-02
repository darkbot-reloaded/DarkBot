package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.objects.Gui;

/**
 * API for special event: Mimesis Mutiny
 */
public interface EscortAPI extends Gui, API {
    double getTime();
    double getKeys();
}
