package eu.darkbot.api.managers;

import com.formdev.flatlaf.FlatLaf;
import eu.darkbot.api.API;
import eu.darkbot.api.plugin.Module;
import eu.darkbot.api.utils.Version;
import org.jetbrains.annotations.NotNull;

/**
 * Bot's management API
 */
public interface BotAPI extends API {

    /**
     * {@link Version} of the DarkBot.
     */
    Version VERSION = Version.of("1.13.17 beta 59");

    /**
     * @return current used {@link Module}
     */
    Module getModule();

    /**
     * @param module to set
     * @return {@code module}
     */
    Module setModule(@NotNull Module module);

    void setTheme(FlatLaf theme);
}
