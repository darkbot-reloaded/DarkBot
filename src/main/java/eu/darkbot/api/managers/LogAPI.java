package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.utils.Listener;

import java.util.Collection;

/**
 * API for in-game log messages
 */
public interface LogAPI extends API {

    /**
     * Adds {@link Listener} which will be called on each new message.
     *
     * @param onMessage {@link Listener} to be added
     * @return reference to {@code onMessage} listener
     * @see Listener
     */
    Listener<String> addListener(Listener<String> onMessage);

    /**
     * @return last 50 massages from in-game log
     */
    Collection<String> getMessages();
}
