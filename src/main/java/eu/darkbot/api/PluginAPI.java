package eu.darkbot.api;

import eu.darkbot.api.core.Memory;
import eu.darkbot.api.core.Window;
import eu.darkbot.api.managers.BackpageAPI;

public interface PluginAPI {

    <T extends API> T getAPI(Class<T> api) throws IllegalStateException;

    /**
     * @return {@link BackpageAPI}
     * @throws WrongThreadException on access backpage with thread other than backpage one.
     * @see eu.darkbot.api.plugin.Task
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

    class WrongThreadException extends RuntimeException {
        public WrongThreadException(String message) {
            super(message);
        }
    }
}
