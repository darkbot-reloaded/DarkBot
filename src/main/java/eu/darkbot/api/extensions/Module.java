package eu.darkbot.api.extensions;

/**
 * A bot module, that is responsible for the principal task the ship should be performing.
 *
 * Only one Module may run at the time in the bot, so it is crucial that
 * the module will take proper control over all the required bot functions.
 *
 * Some of the responsibilities of the module are:
 *  - Ship movement
 *  - Selecting and attacking targets
 *  - Setting hero modes (in-game config & formation)
 *
 * Other side-tasks like activating abilities can be ran on either
 * {@link Behavior}s or the module, but if you want it to be possible
 * to mix with other modules, it must be a behavior.
 */
public interface Module {

    /**
     * This method will be called every bot tick, generally about every 15ms
     * as long as the bot is set to be running by the user and the ships is
     * valid and on the map, ready to do stuff.
     *
     * It must execute in a non-blocking way, some examples:
     *  - No network lookups
     *  - No long-running tasks
     *  - No sleeping
     *
     * For blocking tasks see {@link Task} instead
     */
    void onTickModule();

    /**
     * This method will be called every bot tick, generally about every 15ms
     * any time that the {@link #onTickModule()} method wouldn't run, because
     * either the bot is stopped by the user, or invalid in some other way,
     * like the game is refreshing or still loading.
     *
     * It must execute in a non-blocking way, some examples:
     *  - No network lookups
     *  - No long-running tasks
     *  - No sleeping
     *
     * For blocking tasks see {@link Task} instead
     */
    default void onTickStopped() {}

    default boolean canRefresh() {
        return true;
    }

    default String getStatus() {
        return null;
    }

    default String getStoppedStatus() {
        return null;
    }
}
