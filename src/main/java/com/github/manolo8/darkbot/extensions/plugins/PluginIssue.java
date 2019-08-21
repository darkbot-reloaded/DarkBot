package com.github.manolo8.darkbot.extensions.plugins;

import java.util.Objects;

public class PluginIssue implements Comparable<PluginIssue> {

    public enum Level {
        INFO, WARNING, ERROR;
    }

    private final String message, description;
    private final Level level;

    public PluginIssue(String message, String description, Level level) {
        Objects.requireNonNull(message, "Message must not be null");
        Objects.requireNonNull(description, "Description must not be null");
        Objects.requireNonNull(level, "Description must not be null");
        this.message = message;
        this.description = description;
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

    public boolean preventsLoading() {
        return level == Level.ERROR;
    }

    public Level getLevel() {
        return level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginIssue that = (PluginIssue) o;
        return level == that.level &&
                message.equals(that.message) &&
                description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, description, level);
    }

    @Override
    public int compareTo(PluginIssue o) {
        if (level != o.level) return o.level.compareTo(level); // Severe levels first
        if (!message.equals(o.message)) return message.compareTo(o.message);
        return description.compareTo(o.description);
    }
}
