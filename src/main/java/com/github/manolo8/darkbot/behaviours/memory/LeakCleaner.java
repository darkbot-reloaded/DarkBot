package com.github.manolo8.darkbot.behaviours.memory;

/**
 * Releases the objects of one memory leak of the game client.
 * <p>
 * Implementations follow the same rules:
 * <ul>
 *     <li>only objects proven to be garbage are touched (removed from the game, and not used by the bot)</li>
 *     <li>objects are found through fixed offsets, like the rest of the bot, and checked (class, identity, owner)
 *     before anything is written: an offset that no longer matches the client finds nothing</li>
 *     <li>only null is written, and only into a field that still holds the expected object, with
 *     {@code replaceLong}: a field typed as a class holds the object's address as is, so any other field,
 *     including an untyped one holding it as an atom, is left alone</li>
 *     <li>before a field is added, the client code reading it is checked to handle null</li>
 *     <li>every loop and collection is bounded, so wrong values can't stall the bot or grow its memory</li>
 * </ul>
 */
interface LeakCleaner {

    /** Milliseconds between two calls of {@link #clean()}. */
    long getIntervalMs();

    /** Finds leaked objects and releases them. */
    void clean();

    /** Forgets the objects found so far: the cleaner was disabled, or a run failed. */
    void reset();

    /** A new client was loaded: forgets everything, also what {@link #reset()} keeps. */
    default void clientChanged() {
        reset();
    }

    /** Objects released since the cleaner was created. */
    long getReleased();

    /** What it releases, in plural, for "released 12 ...". */
    String getReleasedName();
}
