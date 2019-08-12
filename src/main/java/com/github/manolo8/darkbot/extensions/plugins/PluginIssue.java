package com.github.manolo8.darkbot.extensions.plugins;

public class PluginIssue {

    private final String message, description;
    private final boolean preventsLoading;

    public PluginIssue(String message, String description, boolean preventsLoading) {
        this.message = message;
        this.description = description;
        this.preventsLoading = preventsLoading;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

    public boolean preventsLoading() {
        return preventsLoading;
    }
}
