package com.github.manolo8.darkbot.extensions.plugins;

public class PluginException extends Exception {

    private Plugin plugin;

    public PluginException(String message, Throwable cause, Plugin plugin) {
        super(message, cause);
        this.plugin = plugin;
    }

    public PluginException(String message, Plugin plugin) {
        super(message);
        this.plugin = plugin;
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginException(String message) {
        super(message);
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
