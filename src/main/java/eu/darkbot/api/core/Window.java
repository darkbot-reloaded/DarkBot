package eu.darkbot.api.core;

import eu.darkbot.utils.Point;

/**
 * Utility to manage game window
 */
public interface Window {

    /**
     * @return version of native api
     */
    int getVersion();

    /**
     * Triggers reload
     */
    void reload();

    /**
     * Sets size of game window
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
     * @return current process memory usage in MB
     */
    long getMemoryUsage();

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
    void mouseMove(int x, int y);

    default void mouseMove(Point point) {
        mouseMove(point.x, point.y);
    }

    /**
     * Simulates hold of left mouse button
     */
    void mouseDown(int x, int y);

    default void mouseDown(Point point) {
        mouseDown(point.x, point.y);
    }

    /**
     * Simulates release of left mouse button
     */
    void mouseUp(int x, int y);

    default void mouseUp(Point point) {
        mouseUp(point.x, point.y);
    }

    /**
     * Simulates mouse click at x & y coordinates
     */
    void mouseClick(int x, int y);

    default void mouseClick(Point point) {
        mouseClick(point.x, point.y);
    }
}
