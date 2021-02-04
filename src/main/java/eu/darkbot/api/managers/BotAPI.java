package eu.darkbot.api.managers;

import com.formdev.flatlaf.FlatLaf;
import eu.darkbot.api.API;
import eu.darkbot.api.extensions.Module;
import eu.darkbot.utils.Version;
import org.jetbrains.annotations.NotNull;

/**
 * Bot's management API
 */
public interface BotAPI extends API {

    /**
     * {@link Version} of the DarkBot.
     */
    Version VERSION = Version.of("1.13.17 beta 69");

    /**
     * @return avg time of tick in ms.
     */
    double getTickTime();

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
     *
     * @param theme
     */
    void setTheme(FlatLaf theme);
}
