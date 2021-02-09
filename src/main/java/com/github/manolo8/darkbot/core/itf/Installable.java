package com.github.manolo8.darkbot.core.itf;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.api.PluginAPI;

public interface Installable extends eu.darkbot.api.extensions.Installable {
    void install(Main main);
    default void uninstall() {}

    @Override
    default void install(PluginAPI pluginAPI) {
        install(pluginAPI.requireInstance(Main.class));
    }
}
