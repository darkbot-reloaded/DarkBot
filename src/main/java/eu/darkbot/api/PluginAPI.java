package eu.darkbot.api;

import eu.darkbot.api.managers.BackpageAPI;

public interface PluginAPI {

    <T extends API> T getAPI(Class<T> api) throws IllegalArgumentException;

    /**
     * @return {@link BackpageAPI}
     * @throws WrongThreadException on access backpage with thread other than backpage one.
     * @see eu.darkbot.api.plugin.Task
     */
    BackpageAPI getBackpageManager() throws WrongThreadException;

    /**
     * @return avg time of tick in ms.
     */
    double getTickTime();

    class WrongThreadException extends RuntimeException {
        public WrongThreadException(String message) {
            super(message);
        }
    }
}
