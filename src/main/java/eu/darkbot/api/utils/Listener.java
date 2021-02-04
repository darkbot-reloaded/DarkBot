package eu.darkbot.api.utils;

/**
 * Any {@link Listener} need to have strong reference
 * otherwise will be garbage collected.
 *
 * @see java.lang.ref.WeakReference
 */
@FunctionalInterface
public interface Listener<T> {

    void onEvent(T t);
}
