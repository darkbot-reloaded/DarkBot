package eu.darkbot.api.utils;

import java.util.EventListener;

/**
 * Any {@link Listener} need to have strong reference
 * otherwise will be garbage collected.
 */
@FunctionalInterface
public interface Listener<T> extends EventListener {

    void onEvent(T t);
}
