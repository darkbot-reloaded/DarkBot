package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.core.utils.Lazy;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IssueHandler {
    private final IssueHandler parent;

    private final Set<PluginIssue> issues = new TreeSet<>();
    private final Lazy<IssueHandler> listener = new Lazy.NoCache<>();

    public IssueHandler() {
        this(null);
    }

    public IssueHandler(IssueHandler parent) {
        this.parent = parent;
    }

    public void addInfo(String message, String description) {
        add(message, description, PluginIssue.Level.INFO);
    }

    public void addWarning(String message, String description) {
        add(message, description, PluginIssue.Level.WARNING);
    }

    public void addFailure(String message, String description) {
        add(message, description, PluginIssue.Level.ERROR);
    }

    public static String createDescription(Throwable e) {
        return Stream.concat(
                Stream.of("<strong>" + e.toString() + "</strong>"),
                Arrays.stream(e.getStackTrace())
        ).map(Objects::toString)
                .limit(100)
                .collect(Collectors.joining("<br>", "<html>", "</html>"));
    }

    public void add(String message, String description, PluginIssue.Level level) {
        if (this.issues.add(new PluginIssue(message, description, level)))
            listener.send(this);
    }

    public void addListener(Consumer<IssueHandler> listener) {
        this.listener.add(listener);
    }

    public Set<PluginIssue> getIssues() {
        return issues;
    }

    /**
     * @return The highest issue level.
     */
    public PluginIssue.Level getLevel() {
        return issues.isEmpty() ? null : issues.iterator().next().getLevel();
    }

    public boolean canLoad() {
        return getLevel() != PluginIssue.Level.ERROR && (parent == null || parent.canLoad());
    }

}
