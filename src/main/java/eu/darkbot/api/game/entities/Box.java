package eu.darkbot.api.game.entities;

import eu.darkbot.api.config.BoxInfo;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

/**
 * In-game collectable box entity
 */
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
     * @return if box was collected or there was a recent attempt and we're waiting
     * @see Entity#isValid() for a way to know it was fully removed
     */
    boolean isCollected();

    /**
     * Makes an attempt at collecting the box.
     *  - The box must be selectable {@link Entity#isSelectable}.
     *  - The hero must be close to the box
     *  - The method will call {@link Box#setCollected} so subsequent
     *    calls to {@link Box#isCollected()} reflect the update.
     * @return true if the conditions are met and collection is attempted, false if conditions are not met.
     */
    boolean tryCollect();

    /**
     * Makes box being collected for x amount of time * amount of collect tries.
     */
    void setCollected();

    /**
     * @return amount of times this box has been attempted to collect
     */
    int getRetries();

    /**
     * @return instant when {@link Box#isCollected} will start returning true again,
     *         if the box didn't disappear {@link Entity#isValid()}.
     *         {@code null} if no collection attempt was performed.
     */
    @Nullable
    Instant isCollectedUntil();

}
