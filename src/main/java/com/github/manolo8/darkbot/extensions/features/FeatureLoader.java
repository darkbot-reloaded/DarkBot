package com.github.manolo8.darkbot.extensions.features;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.features.decorators.ConfigurableDecorator;
import com.github.manolo8.darkbot.extensions.features.decorators.FeatureDecorator;
import com.github.manolo8.darkbot.extensions.features.decorators.InstallableDecorator;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import java.util.Arrays;
import java.util.List;

public class FeatureLoader {

    private final List<FeatureDecorator<?>> FEATURE_DECORATORS;

    public FeatureLoader(Main main) {
        FEATURE_DECORATORS = Arrays.asList(
                new InstallableDecorator(main),
                new ConfigurableDecorator(main.config.CUSTOM_CONFIGS));
    }

    public <T> T loadFeature(Class<T> clazz) {
        T feature = ReflectionUtils.createInstance(clazz);
        for (FeatureDecorator<?> decorator : FEATURE_DECORATORS) {
            decorator.tryLoad(feature);
        }
        return feature;
    }

    public <T> void unloadFeature(T feature) {
        for (FeatureDecorator<?> decorator : FEATURE_DECORATORS) {
            decorator.tryUnload(feature);
        }
    }

}
