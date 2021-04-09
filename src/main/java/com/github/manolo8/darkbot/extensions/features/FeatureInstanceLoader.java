package com.github.manolo8.darkbot.extensions.features;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.Installable;
import com.github.manolo8.darkbot.extensions.DarkBotPluginApiImpl;
import com.github.manolo8.darkbot.extensions.features.decorators.ConfigurableDecorator;
import com.github.manolo8.darkbot.extensions.features.decorators.FeatureDecorator;
import com.github.manolo8.darkbot.extensions.features.decorators.InstallableDecorator;
import com.github.manolo8.darkbot.extensions.features.decorators.InstructionProviderDecorator;

import java.util.Arrays;
import java.util.List;

/**
 * Takes care of creating instances of features, and calling relevant setup or tear down methods.
 * e.g. calling {@link Installable#install(Main main)} or {@link Configurable#setConfig(Object config)}
 */
class FeatureInstanceLoader {

    private final List<FeatureDecorator<?>> FEATURE_DECORATORS;
    private final ConfigurableDecorator CONFIGURATION_DECORATOR;
    private final DarkBotPluginApiImpl pluginAPI;

    FeatureInstanceLoader(Main main) {
        this.pluginAPI = main.pluginAPI;

        FEATURE_DECORATORS = Arrays.asList(
                new InstallableDecorator(main),
                CONFIGURATION_DECORATOR = new ConfigurableDecorator(main),
                new InstructionProviderDecorator());
    }

    <T> T loadFeature(FeatureDefinition<T> featureDefinition) {
        T feature = pluginAPI.requireInstance(featureDefinition.getClazz());
        for (FeatureDecorator<?> decorator : FEATURE_DECORATORS) {
            decorator.tryLoad(featureDefinition, feature);
        }
        return feature;
    }

    <T> void unloadFeature(T feature) {
        for (FeatureDecorator<?> decorator : FEATURE_DECORATORS) {
            decorator.tryUnload(feature);
        }
    }

    <T> void updateConfig(FeatureDefinition<T> fd) {
        if (fd.getInstance() != null) CONFIGURATION_DECORATOR.tryLoad(fd, fd.getInstance());
    }

}
