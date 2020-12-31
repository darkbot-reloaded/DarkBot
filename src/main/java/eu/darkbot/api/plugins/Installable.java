package eu.darkbot.api.plugins;

import eu.darkbot.api.PluginAPI;

public interface Installable {

    void install(PluginAPI pluginAPI);

    void uninstall();
}
