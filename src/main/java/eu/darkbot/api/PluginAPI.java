package eu.darkbot.api;

import eu.darkbot.api.managers.BackpageAPI;
import eu.darkbot.utils.WrongThreadException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PluginAPI {

    /**
     * @param api to get
     * @param <T> type of api which extends {@link API}
     * @return instance of given {@link API} type
     */
    @Nullable <T extends API> T getAPI(@NotNull Class<T> api);

    /**
     * This method makes you sure that returned instance of given type is not null,
     * otherwise exception will be thrown on init time.
     *
     * @param api to get
     * @param <T> type of api which extends {@link API}
     * @return instance of given {@link API} type
     * @throws IllegalArgumentException if class type was not found.
     */
    @NotNull <T extends API> T requireAPI(@NotNull Class<T> api) throws UnsupportedOperationException;

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
}
