package com.github.manolo8.darkbot.extensions.features.decorators;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import com.google.gson.JsonElement;

import java.lang.reflect.ParameterizedType;
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
        Class<?> configClass = findConfigType(obj.getClass());
        // FIXME: add issue to feature
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

    private Class findConfigType(Class clazz) {
        for (Type itf : clazz.getGenericInterfaces()) {
            if (!(itf instanceof ParameterizedType)) continue;
            ParameterizedType paramType = (ParameterizedType) itf;
            if (paramType.getRawType() == Configurable.class)
                return (Class) paramType.getActualTypeArguments()[0];
        }
        Class parent = clazz.getSuperclass();
        if (parent != null) return findConfigType(parent);
        return null;
    }

    private JsonElement toJsonElement(Object config) {
        return config instanceof JsonElement ? (JsonElement) config : Main.GSON.toJsonTree(config);
    }

    private <T> T toConfig(Object config, Class<T> type) {
        if (config == null) return ReflectionUtils.createInstance(type);
        return type.isInstance(config) ? type.cast(config) : Main.GSON.fromJson(toJsonElement(config), type);
    }

}
