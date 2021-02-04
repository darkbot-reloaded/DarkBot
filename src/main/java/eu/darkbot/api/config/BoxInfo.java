package eu.darkbot.api.config;

import eu.darkbot.api.entities.Box;

/**
 * Predefined settings for {@link Box} customized by user.
 */
public interface BoxInfo {

    boolean shouldCollect();

    int getWaitTime();

    int getPriority();
}
