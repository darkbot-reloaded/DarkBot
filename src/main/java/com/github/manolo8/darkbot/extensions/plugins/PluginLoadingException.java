package com.github.manolo8.darkbot.extensions.plugins;

public class PluginLoadingException extends Exception {

    private Plugin plugin;

    public PluginLoadingException(String message, Throwable cause, Plugin plugin) {
        super(message, cause);
        this.plugin = plugin;
    }

    public PluginLoadingException(String message, Plugin plugin) {
        super(message);
        this.plugin = plugin;
    }

    public PluginLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginLoadingException(String message) {
        super(message);
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
