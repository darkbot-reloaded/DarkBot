package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractPluginFeatureHandler<T, D> implements PluginListener {

    protected final PluginHandler pluginHandler;
    protected final LinkedHashMap<String, D> FEATURES_BY_ID = new LinkedHashMap<>();
    protected final Map<String, Class<? extends T>> FEATURE_CLASSES_BY_ID = new LinkedHashMap<>();
    protected final Map<Class<? extends T>, T> FEATURE_INSTANCES = new HashMap<>();
    protected void register(Class<? extends T> feature, D definition) {
        String id = feature.getCanonicalName();
        FEATURES_BY_ID.put(id, definition);
        FEATURE_CLASSES_BY_ID.put(id, feature);
    }

    public AbstractPluginFeatureHandler(PluginHandler pluginHandler) {
        this.pluginHandler = pluginHandler;
        pluginHandler.addListener(this);
    }

    @Override
    public void beforeLoad() {
        FEATURES_BY_ID.clear();
        FEATURE_CLASSES_BY_ID.clear();
        FEATURE_INSTANCES.clear();
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
    protected abstract void registerFeature(Class<? extends T> clazz);
    protected abstract void afterRegistration();

    protected abstract T getDefault();

    public T getFeature(String id) {
        synchronized (pluginHandler) {
            Class<? extends T> featureClass = FEATURE_CLASSES_BY_ID.get(id);
            if (featureClass == null) {
                Popups.showMessageAsync("Error", "Failed to find feature " + id + ", using default", JOptionPane.ERROR_MESSAGE);
                return getDefault();
            }
            try {
                return FEATURE_INSTANCES.computeIfAbsent(featureClass, ReflectionUtils::createInstance);
            } catch (Exception e) {
                Popups.showMessageAsync("Error", "Failed to load feature " + id + ", using default", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
            return getDefault();
        }
    }

    public D getFeatureDefinition(String id) {
        synchronized (pluginHandler) {
            return FEATURES_BY_ID.get(id);
        }
    }

}
