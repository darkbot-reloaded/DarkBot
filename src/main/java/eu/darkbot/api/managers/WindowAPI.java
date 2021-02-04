package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.utils.Area;
import eu.darkbot.api.objects.Point;

/**
 * Utility to manage game window
 */
public interface WindowAPI extends API {

    /**
     * @return version of native api
     */
    int getVersion();

    /**
     * Triggers reload
     */
    void reload();

    /**
     * Sets size of game API window
     */
    void setSize(int width, int height);

    /**
     * Makes browser window visible.
     */
    void setVisible(boolean visible);

    /**
     * Hides window, reduces cpu usage.
     * For some is bugged
     */
    void setMinimized(boolean visible);

    /**
     * Sends string to game window
     *
     * @param text to send
     */
    void sendText(String text);

    /**
     * Simulates key press and release
     *
     * @param keyCode to send
     */
    void keyClick(int keyCode);

    default void keyClick(Character keyCode) {
        if (keyCode != null) keyClick(keyCode.charValue());
    }

    /**
     * Moves mouse to x & y coordinates of game window
     */
    void mouseMove(int x, int y);  //cast to int here or in implementation?

    default void mouseMove(Point point) {
        mouseMove((int) point.getX(), (int) point.getY());
    }

    /**
     * Simulates hold of left mouse button
     */
    void mouseDown(int x, int y);

    default void mouseDown(Point point) {
        mouseDown((int) point.getX(), (int) point.getY());
    }

    /**
     * Simulates release of left mouse button
     */
    void mouseUp(int x, int y);

    default void mouseUp(Point point) {
        mouseUp((int) point.getX(), (int) point.getY());
    }

    /**
     * Simulates mouse click at x & y coordinates
     */
    void mouseClick(int x, int y);

    default void mouseClick(Point point) {
        mouseClick((int) point.getX(), (int) point.getY());
    }
}
