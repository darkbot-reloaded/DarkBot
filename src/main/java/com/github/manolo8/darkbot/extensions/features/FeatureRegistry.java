package com.github.manolo8.darkbot.extensions.features;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginListener;
import com.github.manolo8.darkbot.modules.CollectorModule;
import com.github.manolo8.darkbot.modules.LootModule;
import com.github.manolo8.darkbot.modules.LootNCollectorModule;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class FeatureRegistry implements PluginListener {

    protected final PluginHandler pluginHandler;
    protected final Map<String, FeatureDefinition<?>> FEATURES_BY_ID = new LinkedHashMap<>();
    protected final FeatureLoader featureLoader;

    private final List<Class<?>> DEFAULT_FEATURES = Arrays.asList(CollectorModule.class, LootModule.class, LootNCollectorModule.class);

    public FeatureRegistry(Main main, PluginHandler pluginHandler) {
        this.pluginHandler = pluginHandler;
        this.featureLoader = new FeatureLoader(main);
        pluginHandler.addListener(this);
    }

    @Override
    public void beforeLoad() {
        FEATURES_BY_ID.values()
                .stream()
                .map(FeatureDefinition::getInstance)
                .filter(Objects::nonNull)
                .forEach(featureLoader::unloadFeature);
        FEATURES_BY_ID.clear();

        for (Class<?> feature : DEFAULT_FEATURES) registerNativeFeature(feature);
    }

    @Override
    public void afterLoad() {
        pluginHandler.LOADED_PLUGINS.forEach(pl ->
                Arrays.stream(pl.getDefinition().features)
                        .forEach(feature -> registerPluginFeature(pl, feature)));
    }

    private void registerNativeFeature(Class<?> clazz) {
        FEATURES_BY_ID.put(clazz.getCanonicalName(), new FeatureDefinition<>(null, clazz));
    }

    private void registerPluginFeature(Plugin plugin, String clazzName) {
        try {
            FEATURES_BY_ID.put(clazzName, new FeatureDefinition<>(plugin, (Class<?>) pluginHandler.PLUGIN_CLASS_LOADER.loadClass(clazzName)));
        } catch (ClassNotFoundException e) {
            plugin.getIssues().addWarning("Feature failed to load", clazzName + " couldn't be registered properly: " + e.getMessage());
        }
    }

    public <T> Optional<T> getFeature(String id) {
        synchronized (pluginHandler) {
            try {
                FeatureDefinition<T> feature = getFeatureDefinition(id);
                if (feature == null || !feature.canLoad()) return Optional.empty();

                T instance = feature.getInstance();
                if (instance != null) return Optional.of(instance);

                feature.setInstance(instance = featureLoader.loadFeature(feature.getClazz()));
                return Optional.of(instance);
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }

    public <T> Optional<T> getFeature(String id, Class<T> type) {
        return getFeature(id);
    }

    public <T> List<FeatureDefinition<T>> getFeatures(Class<T> type) {
        //noinspection unchecked
        return FEATURES_BY_ID
                .values()
                .stream()
                .filter(FeatureDefinition::canLoad)
                .filter(fd -> type.isAssignableFrom(fd.getClazz()))
                .map(fd -> (FeatureDefinition<T>) fd)
                .collect(Collectors.toList());
    }

    public List<FeatureDefinition<?>> getFeatures(Plugin plugin) {
        return FEATURES_BY_ID
                .values()
                .stream()
                .filter(fd -> fd.getPlugin() == plugin)
                .collect(Collectors.toList());
    }


    public <T> FeatureDefinition<T> getFeatureDefinition(String id) {
        synchronized (pluginHandler) {
            //noinspection unchecked
            return (FeatureDefinition<T>) FEATURES_BY_ID.get(id);
        }
    }

}
