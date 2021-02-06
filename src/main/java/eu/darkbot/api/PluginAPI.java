package eu.darkbot.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PluginAPI extends API.Singleton {

    /**
     * @param api to get
     * @param <T> type of api which extends {@link API}
     * @return instance of given {@link API} type, null if not present
     */
    @Nullable <T extends API> T getAPI(@NotNull Class<T> api);

    /**
     * This method makes you sure that returned instance of given type is not null,
     * otherwise exception will be thrown.
     *
     * @param api to get
     * @param <T> type of api which extends {@link API}
     * @return instance of given {@link API} type
     * @throws UnsupportedOperationException if given api isn't supported
     */
    @NotNull <T extends API> T requireAPI(@NotNull Class<T> api) throws UnsupportedOperationException;

    /**
     * This method will attempt to create an instance of the asked class, by calling the
     * appropriate constructor
     *
     * @param clazz The class to create an instance of
     * @param <T> type of instance to create
     * @return instance of given type, if a suitable constructor was found
     * @throws UnsupportedOperationException if given api isn't supported
     */
    @NotNull <T> T requireInstance(@NotNull Class<T> clazz) throws UnsupportedOperationException;

}
