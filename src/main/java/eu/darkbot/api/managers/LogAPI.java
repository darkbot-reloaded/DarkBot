package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.objects.Gui;

import java.util.Collection;

/**
 * API for in-game log messages
 */
public interface LogAPI extends Gui, API {
    /**
     * Gets the most recent log message
     *
     * @return the most recent message in your in-game log
     */
    String getLastMessage();

    /**
     * @return last 50 massages from in-game log
     */
    Collection<String> getMessages();
}
