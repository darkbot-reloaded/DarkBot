package eu.darkbot.api;

import eu.darkbot.api.core.Memory;
import eu.darkbot.api.core.Window;
import eu.darkbot.api.managers.BackpageAPI;

public interface PluginAPI {

    <T extends API> T getAPI(Class<T> api);

    /**
     * @return {@link BackpageAPI}
     * @throws WrongThreadException on access backpage with thread other than backpage one.
     */
    BackpageAPI getBackpageManager() throws WrongThreadException;

    /**
     * @return {@link Memory}
     */
    Memory getMemoryUtility();

    /**
     * @return {@link Window}
     */
    Window getWindowUtility();

    /**
     * Gets current ping.
     * Ping is updated every 15 seconds.
     *
     * @return current ping in milliseconds.
     */
    int getPing();

    class WrongThreadException extends RuntimeException {
        public WrongThreadException(String message) {
            super(message);
        }
    }
}
