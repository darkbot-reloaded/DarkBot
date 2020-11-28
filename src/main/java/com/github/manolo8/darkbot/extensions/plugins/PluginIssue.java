package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.utils.I18n;

import java.util.Objects;

public class PluginIssue implements Comparable<PluginIssue> {

    public enum Level {
        INFO, WARNING, ERROR
    }

    private final String messageKey, description;
    private final Level level;

    public PluginIssue(String messageKey, String description, Level level) {
        Objects.requireNonNull(messageKey, "Message must not be null");
        Objects.requireNonNull(description, "Description must not be null");
        Objects.requireNonNull(level, "Description must not be null");
        this.messageKey = messageKey;
        this.description = description;
        this.level = level;
    }

    public String getMessage() {
        return I18n.getOrDefault(messageKey, messageKey);
    }

    public String getMessageKey() {
        return messageKey;
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
                messageKey.equals(that.messageKey) &&
                description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageKey, description, level);
    }

    @Override
    public int compareTo(PluginIssue o) {
        if (level != o.level) return o.level.compareTo(level); // Severe levels first
        if (!messageKey.equals(o.messageKey)) return messageKey.compareTo(o.messageKey);
        return description.compareTo(o.description);
    }
}
