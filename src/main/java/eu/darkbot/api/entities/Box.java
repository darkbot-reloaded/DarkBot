package eu.darkbot.api.entities;

import java.time.LocalDateTime;

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
     * @return true if box is collected or
     * there was a try to collect it and currently is in timer.
     *
     * @see Entity#isRemoved()
     */
    boolean isCollected();

    /**
     * @return time until box is marked as collected
     */
    LocalDateTime isCollectedUntil();

    /**
     * Makes box being collected for x amount of time * amount of collect tries.
     */
    void setCollected();
}
