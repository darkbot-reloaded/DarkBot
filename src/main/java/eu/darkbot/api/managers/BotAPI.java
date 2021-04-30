package eu.darkbot.api.managers;

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
    Version VERSION = Version.of("1.13.17 beta 94");

    /**
     * @return avg time of tick in ms.
     */
    double getTickTime();

    /**
     * @return current used {@link Module}
     */
    Module getModule();

    /**
     * Sets the currently running module in the bot.
     * Keep in mind that any pause & restart will wipe this module and re-set the user defined module.
     *
     * This is mainly useful to install {@link eu.darkbot.logic.modules.TemporalModule}s that
     * will take over the control of the bot for a small amount of time before delegating back
     * to the {@link Module} set by the user.
     *
     * Examples:
     *  - A normal module may set a map traveling module to go to the working map
     *  - A behavior wanting temporal control over movement can install a temporal module that does that
     *
     * @param module The module to set, often a {@link eu.darkbot.logic.modules.TemporalModule}
     * @return The same module that was passed in, useful to chain methods.
     */
    <M extends Module> M setModule(@Nullable M module);

}
