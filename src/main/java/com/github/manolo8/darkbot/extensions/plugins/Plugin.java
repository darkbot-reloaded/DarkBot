package com.github.manolo8.darkbot.extensions.plugins;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Plugin {

    private final URL jar;

    private PluginDefinition definition;
    private List<PluginIssue> issues = new ArrayList<>();
    private boolean canLoad = true;

    public Plugin(URL jar) {
        this.jar = jar;
    }

    public URL getJar() {
        return jar;
    }

    public void setDefinition(PluginDefinition definition) {
        this.definition = definition;
    }

    public PluginDefinition getDefinition() {
        return definition;
    }

    public void addWarning(String message, String description) {
        this.issues.add(new PluginIssue(message, description, false));
    }

    public void addFailure(String message, String description) {
        this.issues.add(new PluginIssue(message, description, true));
        canLoad = false;
    }

    public boolean canLoad() {
        return canLoad;
    }

    public List<PluginIssue> getIssues() {
        return issues;
    }

    public String getName() {
        return definition != null ? definition.name : new File(jar.getFile()).getName();
    }

}
