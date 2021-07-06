package eu.darkbot.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PluginAPI extends API.Singleton {

    /**
     * Gets the instance of an API interface, if one has been registered by the bot implementation.
     * If no implementation has been defined for the API, null is returned.
     *
     * @param api API interface to get
     * @param <T> API type, must extend {@link API}
     * @return an instance of the implementation for the given {@link API}, null if not available
     */
    @Nullable <T extends API> T getAPI(@NotNull Class<T> api);

    /**
     * Gets the instance of an API interface, if one has been registered by the bot implementation.
     * If no implementation has been defined for the API, {@link UnsupportedOperationException} is thrown.
     *
     * @param api API interface to get
     * @param <T> API type, must extend {@link API}
     * @return an instance of the implementation for the given {@link API}
     * @throws UnsupportedOperationException if given api isn't supported
     */
    @NotNull <T extends API> T requireAPI(@NotNull Class<T> api) throws UnsupportedOperationException;

    /**
     * Creates an instance of the asked class, by calling the constructor filling-in APIs.
     * If multiple constructors exist, the one annotated with @Inject will be used.
     * Constructor parameters will be filled-in recursively, so if the constructor requires
     * an api or instance of some other type, it will also be created.
     *
     * @param clazz The class to create an instance of
     * @param <T> type of instance to create
     * @return instance of given type, if a suitable constructor was found
     * @throws UnsupportedOperationException if given api isn't supported
     */
    @NotNull <T> T requireInstance(@NotNull Class<T> clazz) throws UnsupportedOperationException;

}
