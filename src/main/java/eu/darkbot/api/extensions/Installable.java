package eu.darkbot.api.extensions;

import eu.darkbot.api.PluginAPI;

public interface Installable {

    void install(PluginAPI pluginAPI);

    void uninstall();
}
