package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.PluginInfo;

import java.io.File;
import java.net.URL;

public class Plugin {

    private final URL jar;

    private PluginDefinition definition;
    private PluginInfo info;
    private IssueHandler issues = new IssueHandler();

    public Plugin(URL jar) {
        this.jar = jar;
    }

    public URL getJar() {
        return jar;
    }

    public void setDefinition(PluginDefinition definition) {
        this.definition = definition;
        info = ConfigEntity.INSTANCE.getPluginInfo(definition);
    }

    public PluginDefinition getDefinition() {
        return definition;
    }

    public PluginInfo getInfo() {
        return info;
    }

    public IssueHandler getIssues() {
        return issues;
    }

    public String getName() {
        return definition != null ? definition.name : new File(jar.getFile()).getName();
    }

}
