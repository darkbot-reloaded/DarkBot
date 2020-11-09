package eu.darkbot.api.utils;

import java.util.EventListener;

/**
 * Any {@link ChangeListener} need to have strong reference
 * otherwise will be garbage collected.
 */
public interface ChangeListener<T> extends EventListener {

    void onChange(T t);
}
