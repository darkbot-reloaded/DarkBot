package com.github.manolo8.darkbot.extensions.plugins;

import java.util.Objects;

public class PluginIssue implements Comparable<PluginIssue> {

    private final String message, description;
    private final boolean preventsLoading;

    public PluginIssue(String message, String description, boolean preventsLoading) {
        Objects.requireNonNull(message, "Message must not be null");
        Objects.requireNonNull(description, "Description must not be null");
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginIssue that = (PluginIssue) o;
        return preventsLoading == that.preventsLoading &&
                message.equals(that.message) &&
                description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, description, preventsLoading);
    }

    @Override
    public int compareTo(PluginIssue o) {
        if (preventsLoading != o.preventsLoading) return Boolean.compare(preventsLoading, o.preventsLoading);
        if (!message.equals(o.message)) return message.compareTo(o.message);
        return description.compareTo(o.description);
    }
}
