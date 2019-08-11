package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.gui.utils.Popups;

import javax.swing.*;
import java.util.Arrays;
import java.util.LinkedHashMap;

public abstract class AbstractPluginFeatureHandler<T> implements PluginListener {

    protected final PluginHandler pluginHandler;
    protected final LinkedHashMap<String, String> FEATURE_NAMES_BY_ID = new LinkedHashMap<>();
    protected final LinkedHashMap<String, String> FEATURE_DESCRIPTIONS_BY_ID = new LinkedHashMap<>();
    protected final LinkedHashMap<String, Class<? extends T>> FEATURE_CLASSES_BY_ID = new LinkedHashMap<>();
    protected void register(String name, String description, Class<? extends T> feature) {
        String id = feature.getCanonicalName();
        FEATURE_NAMES_BY_ID.put(id, name);
        FEATURE_DESCRIPTIONS_BY_ID.put(id, description);
        FEATURE_CLASSES_BY_ID.put(id, feature);
    }

    public AbstractPluginFeatureHandler(PluginHandler pluginHandler) {
        this.pluginHandler = pluginHandler;
        pluginHandler.addListener(this);
    }

    @Override
    public void beforeLoad() {
        FEATURE_NAMES_BY_ID.clear();
        FEATURE_CLASSES_BY_ID.clear();
        registerDefaults();
    }

    @Override
    public void afterLoad() {
        pluginHandler.LOADED_PLUGINS.stream()
                .map(Plugin::getDefinition)
                .flatMap(d -> Arrays.stream(getFeatures(d)))
                .forEach(this::registerPluginFeature);
        afterRegistration();
    }

    private void registerPluginFeature(String clazzName) {
        try {
            //noinspection unchecked
            registerFeature((Class<T>) pluginHandler.PLUGIN_CLASS_LOADER.loadClass(clazzName));
        } catch (Exception e) {
            System.err.println("Could not register feature: " + clazzName);
            e.printStackTrace();
        }
    }

    protected abstract void registerDefaults();
    protected abstract String[] getFeatures(PluginDefinition plugin);
    protected abstract void registerFeature(Class<T> clazz);
    protected abstract void afterRegistration();

    protected abstract T getDefault();

    public T getFeature(String id) {
        if (pluginHandler.isLoading) return getDefault();
        Class<? extends T> featureClass = FEATURE_CLASSES_BY_ID.get(id);
        if (featureClass == null) {
            Popups.showMessageAsync("Error", "Failed to find feature " + id + ", using default", JOptionPane.ERROR_MESSAGE);
            return getDefault();
        }
        try {
            return featureClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            Popups.showMessageAsync("Error", "Failed to load feature " + id + ", using default", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return getDefault();
    }

}
