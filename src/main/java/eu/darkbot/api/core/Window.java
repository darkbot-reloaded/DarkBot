package eu.darkbot.api.core;

import eu.darkbot.api.objects.Point;

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
    void mouseMove(double x, double y);  //cast to int here or in implementation?

    default void mouseMove(Point point) {
        mouseMove(point.getX(), point.getY());
    }

    /**
     * Simulates hold of left mouse button
     */
    void mouseDown(double x, double y);

    default void mouseDown(Point point) {
        mouseDown(point.getX(), point.getY());
    }

    /**
     * Simulates release of left mouse button
     */
    void mouseUp(double x, double y);

    default void mouseUp(Point point) {
        mouseUp(point.getX(), point.getY());
    }

    /**
     * Simulates mouse click at x & y coordinates
     */
    void mouseClick(double x, double y);

    default void mouseClick(Point point) {
        mouseClick(point.getX(), point.getY());
    }
}
