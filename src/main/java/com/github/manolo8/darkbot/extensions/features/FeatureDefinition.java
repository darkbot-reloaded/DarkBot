package com.github.manolo8.darkbot.extensions.features;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import eu.darkbot.api.extensions.FeatureInfo;
import eu.darkbot.api.extensions.PluginInfo;

import java.util.function.Consumer;

public class FeatureDefinition<T> implements FeatureInfo<T> {

    private final Plugin plugin;
    private final Class<T> clazz;
    private final Feature feature;
    private final IssueHandler issues;

    private final String id;
    private final String name;
    private final String description;

    private final Lazy<FeatureDefinition<T>> listener = new Lazy.NoCache<>();

    private T instance;

    public FeatureDefinition(Plugin plugin, Class<T> clazz) {
        this.plugin = plugin;
        this.clazz = clazz;
        this.feature = clazz.getAnnotation(Feature.class);
        this.issues = new IssueHandler(plugin == null ? null : plugin.getIssues());

        this.id = clazz.getCanonicalName();
        this.name = feature.name();
        this.description = feature.description();

        if (plugin != null
                && !plugin.getInfo().ENABLED_FEATURES.contains(id)
                && !plugin.getInfo().DISABLED_FEATURES.contains(id)) {
            setStatusInternal(Module.class.isAssignableFrom(clazz) || feature.enabledByDefault());
        }
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
        listener.send(this);
    }

    public void setStatus(boolean enabled) {
        if (setStatusInternal(enabled)) {
            ConfigEntity.changed();
            sendUpdate();
        }
    }

    public void sendUpdate() {
        listener.send(this);
    }

    private boolean setStatusInternal(boolean enabled) {
        if (enabled) return plugin.getInfo().ENABLED_FEATURES.add(id) | plugin.getInfo().DISABLED_FEATURES.remove(id);
        else return plugin.getInfo().ENABLED_FEATURES.remove(id) | plugin.getInfo().DISABLED_FEATURES.add(id);
    }

    public void addStatusListener(Consumer<FeatureDefinition<T>> listener) {
        this.listener.add(listener);
    }

    public boolean isEnabled() {
        return plugin == null || plugin.getInfo().ENABLED_FEATURES.contains(id);
    }

    public void setEnabled(boolean enabled) {
        this.setStatus(enabled);
    }

    @Override
    public Class<T> getFeatureClass() {
        return getClazz();
    }

    @Override
    public PluginInfo getPluginInfo() {
        return plugin;
    }

    public boolean canLoad() {
        return issues.canLoad() && isEnabled();
    }

}
