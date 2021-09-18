package com.github.manolo8.darkbot.extensions.features;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.features.handlers.BehaviourHandler;
import com.github.manolo8.darkbot.extensions.features.handlers.ExtraMenuHandler;
import com.github.manolo8.darkbot.extensions.features.handlers.FeatureHandler;
import com.github.manolo8.darkbot.extensions.features.handlers.ModuleHandler;
import com.github.manolo8.darkbot.extensions.features.handlers.NpcExtraHandler;
import com.github.manolo8.darkbot.extensions.features.handlers.TaskHandler;
import eu.darkbot.api.PluginAPI;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private final AtomicBoolean updating = new AtomicBoolean();

    public FeatureRegisterHandler(PluginAPI api, FeatureRegistry featureRegistry) {
        this.featureRegistry = featureRegistry;
        this.FEATURE_HANDLERS = Arrays.asList(
                new ModuleHandler(),
                new BehaviourHandler(api.requireInstance(Main.class), featureRegistry),
                new TaskHandler(api.requireInstance(Main.class), featureRegistry),
                new NpcExtraHandler(featureRegistry),
                new ExtraMenuHandler(featureRegistry)
        );
    }

    Stream<Class<?>> getNativeFeatures() {
        return FEATURE_HANDLERS.stream().flatMap(fr -> Arrays.stream(fr.getNativeFeatures()));
    }

    void update() {
        if (updating.compareAndSet(false, true)) {
            FEATURE_HANDLERS.forEach(this::update);
            updating.set(false);
        }
    }

    private <T> void update(FeatureHandler<T> registerer) {
        registerer.update(featureRegistry.getFeatures(registerer.getHandledType()));
    }

}
