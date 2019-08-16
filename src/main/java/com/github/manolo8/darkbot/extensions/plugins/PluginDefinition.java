package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.extensions.util.Version;

public class PluginDefinition {
    private transient final String[] NULL_ARRAY = new String[0];

    // Name of the plugin
    public String name;
    // Author of the plugin
    public String author;
    // Version of the plugin
    public Version version;
    // Minimum version this plugin requires to run
    public Version minVersion;
    // Latest tested version this plugin runs on
    public Version supportedVersion;
    // Array of fully qualified class names of modules
    public String[] features = NULL_ARRAY;
}
