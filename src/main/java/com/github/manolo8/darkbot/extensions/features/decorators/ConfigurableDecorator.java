package com.github.manolo8.darkbot.extensions.features.decorators;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.util.Map;

public class ConfigurableDecorator extends FeatureDecorator<Configurable> {

    private final Map<String, Object> CUSTOM_CONFIGS;

    public ConfigurableDecorator(Map<String, Object> CUSTOM_CONFIGS) {
        this.CUSTOM_CONFIGS = CUSTOM_CONFIGS;
    }

    @Override
    protected void load(Configurable obj) {
        String id = obj.getClass().getCanonicalName();
        Type[] configParams = ReflectionUtils.findGenericParameters(obj.getClass(), Configurable.class);
        // FIXME: add issue to feature
        if (configParams == null || configParams.length == 0) return;
        Class<?> configClass = (Class) configParams[0];
        if (configClass == null) return;

        Object config = toConfig(CUSTOM_CONFIGS.get(id), configClass);
        CUSTOM_CONFIGS.put(id, config);

        //noinspection unchecked
        obj.setConfig(config);
    }

    @Override
    protected void unload(Configurable obj) {
        String id = obj.getClass().getCanonicalName();
        CUSTOM_CONFIGS.put(id, toJsonElement(CUSTOM_CONFIGS.get(id)));
    }

    private JsonElement toJsonElement(Object config) {
        return config instanceof JsonElement ? (JsonElement) config : Main.GSON.toJsonTree(config);
    }

    private <T> T toConfig(Object config, Class<T> type) {
        if (config == null) return ReflectionUtils.createInstance(type);
        return type.isInstance(config) ? type.cast(config) : Main.GSON.fromJson(toJsonElement(config), type);
    }

}
