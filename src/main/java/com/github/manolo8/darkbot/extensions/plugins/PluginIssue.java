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
        String suffix = invokes > 1 ? "(x" + invokes + ")" : "";
        return I18n.getOrDefault(messageKey, messageKey) + suffix;
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

        PluginIssue issue = (PluginIssue) o;

        if (!Objects.equals(messageKey, issue.messageKey)) return false;
        if (!Objects.equals(description, issue.description)) return false;
        if (!Objects.equals(exceptionString, issue.exceptionString))
            return false;
        return level == issue.level;
    }

    @Override
    public int hashCode() {
        int result = messageKey != null ? messageKey.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (exceptionString != null ? exceptionString.hashCode() : 0);
        result = 31 * result + (level != null ? level.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(PluginIssue o) {
        if (level != o.level) return o.level.compareTo(level); // Severe levels first
        if (!messageKey.equals(o.messageKey)) return messageKey.compareTo(o.messageKey);
        return description.compareTo(o.description);
    }
}
