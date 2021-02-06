package eu.darkbot.api.managers;

import com.formdev.flatlaf.FlatLaf;
import eu.darkbot.api.API;
import eu.darkbot.api.extensions.Module;
import eu.darkbot.utils.Version;
import org.jetbrains.annotations.Nullable;

/**
 * Bot's management API
 */
public interface BotAPI extends API.Singleton {

    /**
     * {@link Version} of the DarkBot.
     */
    Version VERSION = Version.of("1.13.17 beta 72 alpha");

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
    <M extends Module> M setModule(@Nullable M module);

    /**
     *
     * @param theme
     */
    void setTheme(FlatLaf theme);
}
