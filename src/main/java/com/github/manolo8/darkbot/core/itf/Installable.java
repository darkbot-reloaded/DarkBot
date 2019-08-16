package com.github.manolo8.darkbot.core.itf;

import com.github.manolo8.darkbot.Main;

public interface Installable {
    void install(Main main);
    default void uninstall() {}
}
