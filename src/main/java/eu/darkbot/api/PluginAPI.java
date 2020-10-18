package eu.darkbot.api;

import eu.darkbot.api.core.Memory;
import eu.darkbot.api.core.Window;
import eu.darkbot.api.managers.BackpageManager;
import eu.darkbot.api.managers.HeroManager;
import eu.darkbot.api.managers.PetManager;

public interface PluginAPI {
    /**
     * @return {@link BackpageManager}
     */
    BackpageManager getBackpageManager();

    /**
     * @return {@link HeroManager}
     */
    HeroManager getHeroManager();

    /**
     * @return {@link PetManager}
     */
    PetManager getPetManager();

    /**
     * @return {@link Memory}
     */
    Memory getMemoryUtility();

    /**
     * @return {@link Window}
     */
    Window getWindowUtility();

    /**
     * Gets current ping.
     * Ping is updated every 15 seconds.
     *
     * @return current ping in milliseconds.
     */
    int getPing();
}
