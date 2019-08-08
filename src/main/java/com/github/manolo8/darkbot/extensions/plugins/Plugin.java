package com.github.manolo8.darkbot.extensions.plugins;

import java.net.URL;

public class Plugin {

    private final PluginDefinition definition;
    private final URL jar;

    public Plugin(PluginDefinition definition, URL jar) {
        this.definition = definition;
        this.jar = jar;
    }

    public PluginDefinition getDefinition() {
        return definition;
    }

    public URL getJar() {
        return jar;
    }
}
