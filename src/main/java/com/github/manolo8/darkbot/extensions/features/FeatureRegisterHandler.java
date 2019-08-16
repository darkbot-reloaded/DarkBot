package com.github.manolo8.darkbot.extensions.features;

import com.github.manolo8.darkbot.extensions.features.handlers.FeatureHandler;
import com.github.manolo8.darkbot.extensions.features.handlers.ModuleHandler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Takes care of registering features to the bot.
 * Includes both taking care of providing native features in the bot, and being able to
 * apply actions when the features are going to be reloaded or after they have been loaded.
 * e.g. Updating the list of available modules in the dropdown selector in the config.
 */
public class FeatureRegisterHandler {

    private final FeatureRegistry featureRegistry;
    private final List<FeatureHandler<?>> FEATURE_HANDLERS;


    public FeatureRegisterHandler(FeatureRegistry featureRegistry) {
        this.featureRegistry = featureRegistry;
        this.FEATURE_HANDLERS = Arrays.asList(
                new ModuleHandler()
        );
    }

    protected Stream<Class> getNativeFeatures() {
        return FEATURE_HANDLERS.stream().flatMap(fr -> Arrays.stream(fr.getNativeFeatures()));
    }

    protected void beforeLoading() {
        FEATURE_HANDLERS.forEach(this::beforeLoad);
    }

    private <T> void beforeLoad(FeatureHandler<T> registerer) {
        registerer.beforeLoading(featureRegistry.getFeatures(registerer.getHandledType()));
    }

    protected void afterLoading() {
        FEATURE_HANDLERS.forEach(this::afterLoad);
    }

    private <T> void afterLoad(FeatureHandler<T> registerer) {
        registerer.afterLoading(featureRegistry.getFeatures(registerer.getHandledType()));
    }

}
