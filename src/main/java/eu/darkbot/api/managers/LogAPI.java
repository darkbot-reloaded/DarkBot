package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.objects.Gui;
import eu.darkbot.api.utils.Listener;

import java.util.Collection;
import java.util.Optional;

/**
 * API for in-game log messages
 */
public interface LogAPI extends Gui, API {

    /**
     * Adds {@link Listener} which will be called on each new message.
     *
     * @param onMessage {@link Listener} to be added
     * @return reference to {@code onMessage} listener
     * @see Listener
     */
    Listener<String> addListener(Listener<String> onMessage);

    /**
     * Gets the most recent log message
     *
     * @return the most recent message in your in-game log
     */
    Optional<String> getLastMessage();

    /**
     * @return last 50 massages from in-game log
     */
    Collection<String> getMessages();
}
