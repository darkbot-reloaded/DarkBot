package com.github.manolo8.darkbot.extensions.features;

import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;

public class FeatureDefinition<T> {

    private final Plugin plugin;
    private final Class<T> clazz;
    private final Feature feature;
    private final IssueHandler issues;

    private final String id;
    private final String name;
    private final String description;

    private T instance;

    public FeatureDefinition(Plugin plugin, Class<T> clazz) {
        this.plugin = plugin;
        this.clazz = clazz;
        this.feature = clazz.getAnnotation(Feature.class);
        this.issues = new IssueHandler(plugin == null ? null : plugin.getIssues());

        this.id = clazz.getCanonicalName();
        this.name = feature.name();
        this.description = feature.description();
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public Feature getFeature() {
        return feature;
    }

    public IssueHandler getIssues() {
        return issues;
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T getInstance() {
        return instance;
    }

    public void setInstance(T instance) {
        this.instance = instance;
    }

    public boolean canLoad() {
        return issues.canLoad() && (plugin == null || !plugin.getInfo().DISABLED_FEATURES.contains(id));
    }

}
