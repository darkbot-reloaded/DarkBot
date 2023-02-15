package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.gui.utils.Strings;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.MathUtils;
import eu.darkbot.api.extensions.IssueHandler;

import java.util.Objects;

public class PluginIssue implements Comparable<PluginIssue>, IssueHandler.Issue {

    public enum Level {
        INFO, WARNING, ERROR
    }

    private final String messageKey, description;
    private final String exceptionString;
    private final Level level;

    private int invokes;

    public PluginIssue(String messageKey, String description, Level level) {
        this(messageKey, description, level, null);
    }

    public PluginIssue(String messageKey, String description, Level level, Throwable cause) {
        Objects.requireNonNull(messageKey, "Message must not be null");
        Objects.requireNonNull(description, "Description must not be null");
        Objects.requireNonNull(level, "Description must not be null");
        this.messageKey = messageKey;
        this.description = description;
        this.level = level;
        this.exceptionString = cause == null ? null : Strings.exceptionToString(cause);
    }

    public String getMessage() {
        String amountStr = invokes > 0 ? "[" + invokes + "] " : "";
        return amountStr + I18n.getOrDefault(messageKey, messageKey);
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public IssueHandler.Level getIssueLevel() {
        return IssueHandler.Level.values()[level.ordinal()];
    }

    public boolean preventsLoading() {
        return level == Level.ERROR;
    }

    public Level getLevel() {
        return level;
    }

    public void increaseAndPrint() {
        if (MathUtils.isPowerOfTen(++invokes) && exceptionString != null) {
            System.err.print("[" + invokes + "] " + exceptionString);
        }
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
