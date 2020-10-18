package eu.darkbot.api.core;

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
     * <br>
     * Moves window off-screen in {@link eu.darkbot.api.DarkBoat} case
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

    void keyClick(char keyCode);

    void keyClick(Character keyCode);

    /**
     * Moves mouse to x & y coordinates of game window
     */
    void mouseMove(int x, int y);

    /**
     * Simulates hold of left mouse button
     */
    void mouseDown(int x, int y);

    /**
     * Simulates release of left mouse button
     */
    void mouseUp(int x, int y);

    /**
     * Simulates mouse click at x & y coordinates
     */
    void mouseClick(int x, int y);
}
