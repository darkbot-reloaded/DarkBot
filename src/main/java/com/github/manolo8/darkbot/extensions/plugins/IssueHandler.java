package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.core.utils.Lazy;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

public class IssueHandler {
    private final IssueHandler parent;

    private final Set<PluginIssue> issues = new TreeSet<>();
    private final Lazy<IssueHandler> listener = new Lazy.NoCache<>();
    private boolean canLoad = true;

    public IssueHandler() {
        this(null);
    }

    public IssueHandler(IssueHandler parent) {
        this.parent = parent;
    }

    public void addWarning(String message, String description) {
        this.issues.add(new PluginIssue(message, description, false));
        listener.send(this);
    }

    public void addFailure(String message, String description) {
        this.issues.add(new PluginIssue(message, description, true));
        canLoad = false;
        listener.send(this);
    }

    public void addListener(Consumer<IssueHandler> listener) {
        this.listener.add(listener);
    }

    public Set<PluginIssue> getIssues() {
        return issues;
    }

    public boolean hasIssues() {
        return !issues.isEmpty();
    }

    public boolean canLoad() {
        return canLoad && (parent == null || parent.canLoad());
    }



}
