package com.github.manolo8.darkbot.extensions.features;

import com.github.manolo8.darkbot.extensions.features.decorators.ConfigurableDecorator;
import com.github.manolo8.darkbot.extensions.features.decorators.FeatureDecorator;
import com.github.manolo8.darkbot.extensions.features.decorators.InstallableDecorator;
import com.github.manolo8.darkbot.extensions.features.decorators.InstructionProviderDecorator;
import com.github.manolo8.darkbot.extensions.features.decorators.ListenerDecorator;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Configurable;
import eu.darkbot.api.extensions.Installable;

import java.util.Arrays;
import java.util.List;

/**
 * Takes care of creating instances of features, and calling relevant setup or tear down methods.
 * e.g. calling {@link Installable#install(PluginAPI main)} or {@link Configurable#setConfig(ConfigSetting config)}
 */
public class FeatureInstanceLoader {

    private final List<FeatureDecorator<?>> FEATURE_DECORATORS;
    private final ConfigurableDecorator CONFIGURATION_DECORATOR;
    private final PluginAPI api;

    public FeatureInstanceLoader(PluginAPI api,
                                 InstallableDecorator installableDecorator,
                                 ConfigurableDecorator configurableDecorator,
                                 ListenerDecorator listenerDecorator,
                                 InstructionProviderDecorator instructionProviderDecorator) {
        this.api = api;

        FEATURE_DECORATORS = Arrays.asList(
                installableDecorator,
                CONFIGURATION_DECORATOR = configurableDecorator,
                listenerDecorator,
                instructionProviderDecorator);
    }

    <T> T loadFeature(FeatureDefinition<T> featureDefinition) {
        T feature = api.requireInstance(featureDefinition.getClazz());
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
