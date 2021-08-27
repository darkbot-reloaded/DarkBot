package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.tree.ConfigBuilder;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import com.google.gson.JsonElement;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.events.EventHandler;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.extensions.Configurable;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.ExtensionsAPI;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

public class ConfigHandler implements ConfigAPI, Listener {

    private final PluginAPI pluginAPI;

    private final ConfigManager loader;
    private final ConfigBuilder builder;

    private final ConfigSetting.Parent<Config> configuration;

    public ConfigHandler(PluginAPI pluginAPI,
                         ConfigManager loader,
                         ConfigBuilder builder) {
        this.pluginAPI = pluginAPI;
        this.loader = loader;
        this.builder = builder;

        this.configuration = builder.of(Config.class, "Configuration", null);
        this.configuration.setValue(loader.getConfig());
    }

    public Config loadConfig(String configName) {
        Config config = loader.loadConfig(configName);
        configuration.setValue(config);
        return config;
    }

    @Override
    public Config getConfig() {
        return configuration.getValue();
    }

    public ConfigSetting.Parent<Config> getConfigurationRoot() {
        return configuration;
    }

    @Override
    public @Nullable <T> ConfigSetting<T> getConfig(String s) {
        return getConfig(configuration, s);
    }

    @Override
    public Set<String> getChildren(String s) {
        ConfigSetting<?> setting = getConfig(s);
        if (setting instanceof ConfigSetting.Parent)
            return ((ConfigSetting.Parent<?>) setting).getChildren().keySet();
        return null;
    }

    public <T> ConfigSetting.Parent<T> getFeatureConfig(FeatureDefinition<?> fd) {
        Class<?> clazz = fd.getClazz();
        Type[] configType;
        if (Configurable.class.isAssignableFrom(clazz)) {
            configType = ReflectionUtils.findGenericParameters(clazz, Configurable.class);
        } else if (com.github.manolo8.darkbot.core.itf.Configurable.class.isAssignableFrom(clazz)) {
            configType = ReflectionUtils.findGenericParameters(clazz,
                    com.github.manolo8.darkbot.core.itf.Configurable.class);
        } else {
            return null;
        }

        if (configType == null || configType.length != 1 || !(configType[0] instanceof Class)) {
            fd.getIssues().addWarning("Config not loaded", "Could not find config type, so it can't be loaded!");
            return null;
        }

        @SuppressWarnings("unchecked")
        Class<T> configClass = (Class<T>) configType[0];
        ConfigSetting.Parent<T> configSetting = builder.of(configClass, fd.getName(), fd.getPluginInfo());

        Map<String, Object> customConfigs = configuration.getValue().CUSTOM_CONFIGS;

        T configObj = toConfig(customConfigs.get(fd.getId()), configClass);
        customConfigs.put(fd.getId(), configObj);

        configSetting.setValue(configObj);
        return configSetting;
    }

    private JsonElement toJsonElement(Object config) {
        return config instanceof JsonElement ? (JsonElement) config : Main.GSON.toJsonTree(config);
    }

    private <T> T toConfig(Object config, Class<T> type) {
        if (config == null) return ReflectionUtils.createInstance(type);
        return type.isInstance(config) ? type.cast(config) : Main.GSON.fromJson(toJsonElement(config), type);
    }

    @SuppressWarnings("unchecked")
    private static <T> ConfigSetting<T> getConfig(ConfigSetting<?> config, String path) {
        String[] paths = path.split("\\.");
        for (String s : paths) {
            if (config instanceof ConfigSetting.Parent)
                config = ((ConfigSetting.Parent<?>) config).getChildren().get(s);
            else
                config = null;

            if (config == null)
                throw new IllegalArgumentException("Configuration not found: " + s + " in " + path);
        }
        return (ConfigSetting<T>) config;
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
