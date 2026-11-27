package com.github.manolo8.darkbot.behaviours.memory;

/**
 * Releases the objects of one memory leak of the game client.
 * <p>
 * Implementations follow the same rules:
 * <ul>
 *     <li>only objects proven to be garbage are touched (removed from the game, and not used by the bot)</li>
 *     <li>only null is written, and only into a field that still points to the expected object</li>
 *     <li>fields are resolved by name; if a name can't be resolved, nothing is written</li>
 *     <li>only fields typed as a class are cleared: untyped fields hold atoms, where 0 is not null</li>
 *     <li>before a field is added, the client code reading it is checked to handle null</li>
 * </ul>
 */
interface LeakCleaner {

    /** Milliseconds between two calls of {@link #clean()}. */
    long getIntervalMs();

    /** Finds leaked objects and releases them. */
    void clean();

    /** Forgets all found objects: the cleaner was disabled, the client reloaded, or a run failed. */
    void reset();
}
