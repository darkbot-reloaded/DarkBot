package com.github.manolo8.darkbot.extensions.features.decorators;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import java.lang.reflect.Type;

public abstract class FeatureDecorator<T> {
    private final Class<T> handledType;

    public FeatureDecorator() {
        Type[] types = ReflectionUtils.findGenericParameters(getClass(), FeatureDecorator.class);
        if (types == null)
            throw new UnsupportedOperationException("Can't initialize feature decorator with no found type: " + getClass().getCanonicalName());

        //noinspection unchecked
        handledType = (Class<T>) types[0];
    }

    public final <F> void tryLoad(FeatureDefinition<F> fd, F obj) {
        if (!handledType.isInstance(obj)) return;
        //noinspection unchecked
        load((FeatureDefinition<T>) fd, (T) obj);
    }
    protected abstract void load(FeatureDefinition<T> fd, T obj);

    public final void tryUnload(Object obj) {
        if (!handledType.isInstance(obj)) return;
        unload(handledType.cast(obj));
    }
    protected abstract void unload(T obj);
}


