package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.utils.Area;
import eu.darkbot.api.objects.Gui;

import java.util.Collection;

public interface GameScreenAPI extends API.Singleton {

    /**
     * @return bounds of game screen
     */
    Area.Rectangle getViewBounds();

    /**
     * @return {@link Collection} of in-game guis
     */
    Collection<? extends Gui> getGuis();

    /**
     * @return in-game FPS.
     */
    int getFps();

    /**
     * @return memory used by the game in MB
     */
    int getMemory();

    /**
     * Tries to zoom in view.
     */
    void zoomIn();

    /**
     * Tries to zoom out view.
     */
    void zoomOut();

    /**
     * Tries to focus keyboard on chat window.
     */
    void focusOnChat();

    /**
     * Will toggle FPS monitoring in-game.
     */
    void toggleMonitoring();

    /**
     * Will try to toggle visibility for all windows in-game.
     */
    void toggleWindows();

    void toggleCategoryBar(boolean visible);

    void toggleProActionBar(boolean visible);
}
