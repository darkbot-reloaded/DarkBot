package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import eu.darkbot.api.API;
import eu.darkbot.api.utils.Inject;

import java.lang.reflect.Type;
import java.util.stream.Stream;

public abstract class FeatureHandler<T> implements API.Singleton {

    protected final Class<T> handledType;
    protected FeatureRegistry featureRegistry;

    public FeatureHandler() {
        this(FeatureHandler.class);
    }

    public FeatureHandler(Class<?> genericSuperclass) {
        Type[] types = ReflectionUtils.findGenericParameters(getClass(), genericSuperclass);
        if (types == null)
            throw new UnsupportedOperationException("Can't initialize feature decorator with no found type: " + getClass().getCanonicalName());

        //noinspection unchecked
        handledType = (Class<T>) types[0];
    }

    @Inject
    public void setFeatureRegistry(FeatureRegistry featureRegistry) {
        this.featureRegistry = featureRegistry;
    }

    public Class<T> getHandledType() {
        return handledType;
    }

    public abstract Class<?>[] getNativeFeatures();
    public abstract void update(Stream<FeatureDefinition<T>> features);

}
