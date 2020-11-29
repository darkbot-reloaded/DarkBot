package eu.darkbot.api;

import eu.darkbot.api.plugin.Module;
import eu.darkbot.api.utils.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PluginAPI {

    /**
     * {@link Version} of the DarkBot.
     */
    Version VERSION = Version.of("1.13.17 beta 59");

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
     * @return current used {@link Module}
     */
    Module getModule();

    /**
     * @param module to set
     * @return {@code module}
     */
    Module setModule(@NotNull Module module);

    /**
     * @return avg time of tick in ms.
     */
    double getTickTime();
}
