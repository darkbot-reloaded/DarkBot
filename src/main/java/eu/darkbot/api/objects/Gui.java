package eu.darkbot.api.objects;

import java.time.Instant;

/**
 * In-game gui.
 */
public interface Gui extends Point {

    /**
     * @return name of the {@link Gui}
     */
    String getName();

    /**
     * @return width of the gui
     */
    double getWidth();

    /**
     * @return height of the gui
     */
    double getHeight();

    /**
     * @return the right x coordinate of the window
     */
    default double getX2() {
        return getX() + getWidth();
    }

    /**
     * @return the bottom y coordinate of the window
     */
    default double getY2() {
        return getY() + getHeight();
    }

    /**
     * @return true if gui window is visible
     */
    boolean isVisible();

    /**
     * Returns true only if state of gui equals visible param
     * and gui animation is done.
     * <p>
     * Changing state of gui takes ~1 second.
     *
     * @param visible should gui be visible
     * @return true if state of gui equals visible param
     */
    boolean setVisible(boolean visible);

    /**
     * Clicks at gui position + plusX/Y
     */
    void click(int plusX, int plusY);

    /**
     * Moves mouse at gui position + plusX/Y
     */
    void hover(int plusX, int plusY);

    /**
     * @return last {@link Instant} where gui was visible
     */
    Instant lastVisibleTime();
}
