package eu.darkbot.api.objects;

public interface Gui extends Point {
    double getWidth(); //or x2, y2?
    double getHeight();

    /**
     * @return true if gui window is visible
     */
    boolean isVisible();

    /**
     * Returns true only if state of gui equals visible param
     * and gui animation is done.
     *
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
}
