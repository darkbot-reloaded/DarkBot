package eu.darkbot.api.extensions;

/**
 * A behavior that will run in the normal tick of the bot
 *
 * There can be any amount of behaviors running side by side on the bot, which is why
 * they should only run things that will not interfere with each other.
 *
 * Some tasks like ship movement are reserved for {@link Module}s to avoid those conflicts.
 */
public interface Behavior {

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
    void onTickBehavior();

    /**
     * This method will be called every bot tick, generally about every 15ms
     * any time that the {@link #onTickBehavior()} method wouldn't run, because
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
    default void onStoppedBehavior() {}
}
