package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.tree.ConfigBuilder;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import com.google.gson.JsonElement;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.events.EventHandler;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.extensions.Configurable;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.ExtensionsAPI;
import eu.darkbot.api.utils.Inject;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ConfigHandler implements ConfigAPI, Listener {

    private final PluginAPI pluginAPI;

    private final ConfigManager loader;
    private final ConfigBuilder builder;
    private final Lazy<String> configChange;

    private final ConfigSetting.Parent<Config> configuration;

    public ConfigHandler(PluginAPI api,
                         ConfigManager loader,
                         ConfigBuilder builder) {
        this.pluginAPI = api;
        this.loader = loader;
        this.builder = builder;
        this.configChange = api.requireInstance(Main.class).configChange;
        this.configuration = builder.of(Config.class, "Configuration", null);
        this.configuration.setValue(loader.getConfig());
    }

    @Inject
    public void init() {
        ConfigEntity.INSTANCE = pluginAPI.requireInstance(ConfigEntity.class);
    }

    public Config loadConfig(String configName) {
        Config config = loader.loadConfig(configName);
        configuration.setValue(config);
        return config;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ConfigSetting.Parent<T> getConfigRoot() {
        return (ConfigSetting.Parent<T>) configuration;
    }

    @Override
    public String getCurrentProfile() {
        return loader.getConfigName();
    }

    @Override
    public List<String> getConfigProfiles() {
        return loader.getAvailableConfigs();
    }

    @Override
    public void setConfigProfile(String profile) {
        configChange.send(profile);
    }

    public <T> ConfigSetting.Parent<T> getFeatureConfig(FeatureDefinition<?> fd) {
        if (this.configuration == null)
            throw new IllegalStateException("Tried to get feature configs before config has fully initialized");

        Class<?> clazz = fd.getClazz();
        if (!Configurable.class.isAssignableFrom(clazz)) return null;

        Type[] configType = ReflectionUtils.findGenericParameters(clazz, Configurable.class);

        // Search for legacy configurable
        if (configType == null && com.github.manolo8.darkbot.core.itf.Configurable.class.isAssignableFrom(clazz))
            configType = ReflectionUtils.findGenericParameters(clazz,
                    com.github.manolo8.darkbot.core.itf.Configurable.class);

        if (configType == null || configType.length != 1 || !(configType[0] instanceof Class)) {
            fd.getIssues().addWarning("Config not loaded", "Could not find config type, so it can't be loaded!");
            return null;
        }

        @SuppressWarnings("unchecked")
        Class<T> configClass = (Class<T>) configType[0];
        ConfigSetting.Parent<T> configSetting = builder.of(configClass, fd.getName(), fd.getPluginInfo());
        configSetting.setValue(getFeatureConfigValue(fd, configClass));
        return configSetting;
    }

    public <T> T getFeatureConfigValue(FeatureDefinition<?> fd, Class<T> configClass) {
        Map<String, Object> customConfigs = configuration.getValue().CUSTOM_CONFIGS;

        T configObj = toConfig(customConfigs.get(fd.getId()), configClass);
        customConfigs.put(fd.getId(), configObj);
        return configObj;
    }

    public <T> void updateFeatureConfig(FeatureDefinition<?> fd) {
        @SuppressWarnings("unchecked")
        ConfigSetting.Parent<T> config = (ConfigSetting.Parent<T>) fd.getConfig();
        if (config == null) return;
        config.setValue(getFeatureConfigValue(fd, config.getType()));
    }

    private JsonElement toJsonElement(Object config) {
        return config instanceof JsonElement ? (JsonElement) config : ConfigManager.GSON.toJsonTree(config);
    }

    private <T> T toConfig(Object config, Class<T> type) {
        if (config == null) return ReflectionUtils.createInstance(type);
        return type.isInstance(config) ? type.cast(config) : ConfigManager.GSON.fromJson(toJsonElement(config), type);
    }

    @EventHandler
    public void onPluginUnload(ExtensionsAPI.PluginLifetimeEvent e) {
        if (e.getStage() == ExtensionsAPI.PluginStage.BEFORE_LOAD) {
            Map<String, Object> customConfigs = configuration.getValue().CUSTOM_CONFIGS;

            // We apply this here to avoid infinite recursion of dependencies.
            // Config handler needs feature registry, and feature registry needs config handler
            FeatureRegistry fr = pluginAPI.requireInstance(FeatureRegistry.class);

            for (FeatureDefinition<?> feature : fr.getFeatures()) {
                ConfigSetting<?> config = feature.getConfig();
                if (config == null) continue;

                customConfigs.put(feature.getId(), toJsonElement(config.getValue()));
            }
        }
    }

}
