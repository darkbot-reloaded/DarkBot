package eu.darkbot.api.entities;

import eu.darkbot.api.config.BoxInfo;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public interface Box extends Entity {

    /**
     * Hash is generated on server.
     * Hash remains the same for entire box life.
     *
     * @return hash of the box
     */
    String getHash();

    /**
     * @return type of the box.
     */
    String getTypeName();

    /**
     * @return {@link BoxInfo} with some user defined settings for {@link Box}
     */
    BoxInfo getInfo();

    /**
     * @return true if box is collected or
     * there was a try to collect it and currently is in timer.
     * @see Entity#isValid()
     */
    boolean isCollected();

    boolean tryCollect();

    /**
     * Makes box being collected for x amount of time * amount of collect tries.
     */
    void setCollected();

    /**
     * @return amount of collect retries
     */
    int getRetries();

    /**
     * @return time until box is marked as collected
     */
    @Nullable
    Instant isCollectedUntil();

}
