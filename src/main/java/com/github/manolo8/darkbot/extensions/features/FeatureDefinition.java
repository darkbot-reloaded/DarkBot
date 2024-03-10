package com.github.manolo8.darkbot.extensions.features;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.extensions.plugins.PluginIssue;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.FeatureInfo;
import eu.darkbot.api.extensions.PluginInfo;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
public class FeatureDefinition<T> implements FeatureInfo<T> {

    private final @Nullable Plugin plugin;
    private final Class<T> clazz;
    private final IssueHandler issues;

    private final String id;
    private final String name;
    private final String description;

    @Getter(AccessLevel.NONE)
    private final Lazy<FeatureDefinition<T>> listener = new Lazy.NoCache<>();
    @Getter(AccessLevel.NONE)
    private final Lazy.Swing<FeatureDefinition<T>> uiListener = new Lazy.Swing<>();

    private final @Nullable ConfigSetting.Parent<?> config;
    private @Nullable T instance;

    public FeatureDefinition(@Nullable Plugin plugin,
                             Class<T> clazz,
                             Function<FeatureDefinition<T>, ConfigSetting.Parent<?>> configBuilder) {
        this.plugin = plugin;
        this.clazz = clazz;
        this.issues = new IssueHandler(plugin == null ? null : plugin.getIssues());

        this.id = clazz.getCanonicalName();
        this.listener.add(uiListener::send);

        boolean enabledByDefault;
        if (clazz.getAnnotation(Feature.class) != null) {
            Feature feature = clazz.getAnnotation(Feature.class);
            this.name = feature.name();
            this.description = feature.description();
            enabledByDefault = feature.enabledByDefault();
        } else {
            eu.darkbot.api.extensions.Feature feature = clazz.getAnnotation(eu.darkbot.api.extensions.Feature.class);
            if (feature == null)
                throw new IllegalStateException("Feature class must be annotated with @Feature: " + clazz.getCanonicalName());
            this.name = feature.name();
            this.description = feature.description();
            enabledByDefault = feature.enabledByDefault();
        }

        if (plugin != null
                && !plugin.getInfo().ENABLED_FEATURES.contains(id)
                && !plugin.getInfo().DISABLED_FEATURES.contains(id)) {
            // Intentionally uses old module class, newer features will
            // always rely exclusively on if set to be enabled by default
            setStatusInternal(Module.class.isAssignableFrom(clazz) || enabledByDefault);
        }

        this.config = configBuilder.apply(this);
    }

    public void setInstance(T instance) {
        this.instance = instance;
        sendUpdate();
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
        if (plugin == null)
            throw new IllegalStateException("Native features cannot be enabled or disabled");
        if (enabled) return plugin.getInfo().ENABLED_FEATURES.add(id) | plugin.getInfo().DISABLED_FEATURES.remove(id);
        else return plugin.getInfo().ENABLED_FEATURES.remove(id) | plugin.getInfo().DISABLED_FEATURES.add(id);
    }

    public void addStatusListener(Consumer<FeatureDefinition<T>> listener) {
        this.listener.add(listener);
    }

    public void addStatusUiListener(Consumer<FeatureDefinition<T>> listener) {
        this.uiListener.add(listener);
    }

    @Override
    public boolean isEnabled() {
        return plugin == null || plugin.getInfo().ENABLED_FEATURES.contains(id);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.setStatus(enabled);
    }

    @Override
    public Class<T> getFeatureClass() {
        return getClazz();
    }

    @Override
    public @Nullable PluginInfo getPluginInfo() {
        return plugin;
    }

    @Override
    public boolean canLoad() {
        return issues.canLoad() && isEnabled();
    }

    @Override
    public @NotNull Set<? extends Issue> getIssueSet() {
        return issues.getIssues();
    }

    @Override
    public void addIssue(@NotNull String message, @NotNull String description, @NotNull Level level) {
        issues.add(message, description, PluginIssue.Level.values()[level.ordinal()]);
    }
}
