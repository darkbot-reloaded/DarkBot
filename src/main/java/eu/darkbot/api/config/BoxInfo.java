package eu.darkbot.api.config;

import eu.darkbot.api.game.entities.Box;

/**
 * Predefined settings for {@link Box} customized by user.
 */
public interface BoxInfo {

    /**
     * @return True if the user wants to collect these type of boxes, false otherwise
     */
    boolean shouldCollect();

    /**
     * @return How long the user wants to wait for the box to be picked up in milliseconds
     */
    int getWaitTime();

    /**
     * @return How important this box is to the user, the lower number is more important
     */
    int getPriority();
}
