package com.github.manolo8.darkbot.extensions.plugins;

import java.util.Set;
import java.util.TreeSet;

public class IssueHandler {
    private final IssueHandler parent;

    private final Set<PluginIssue> issues = new TreeSet<>();
    private boolean canLoad = true;

    public IssueHandler() {
        this(null);
    }

    public IssueHandler(IssueHandler parent) {
        this.parent = parent;
    }

    public void addWarning(String message, String description) {
        this.issues.add(new PluginIssue(message, description, false));
    }

    public void addFailure(String message, String description) {
        this.issues.add(new PluginIssue(message, description, true));
        canLoad = false;
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
