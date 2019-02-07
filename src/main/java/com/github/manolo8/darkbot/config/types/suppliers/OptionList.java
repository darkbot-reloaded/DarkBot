package com.github.manolo8.darkbot.config.types.suppliers;

import java.util.Collection;

public interface OptionList<T> {
    T getValue(String text);
    String getText(T value);
    Collection<String> getOptions();
}
