package com.github.manolo8.darkbot.extensions.features;

import com.github.manolo8.darkbot.extensions.features.handlers.*;
import eu.darkbot.api.API;
import eu.darkbot.api.PluginAPI;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Takes care of registering features to the bot.
 * Includes both taking care of providing native features in the bot, and being able to
 * apply actions when the features are going to be reloaded or after they have been loaded.
 * e.g. Updating the list of available modules in the dropdown selector in the config.
 */
public class FeatureRegisterHandler implements API.Singleton {

    private final FeatureRegistry featureRegistry;
    private final List<FeatureHandler<?>> FEATURE_HANDLERS;

    private final AtomicBoolean updating = new AtomicBoolean();

    public FeatureRegisterHandler(PluginAPI api, FeatureRegistry featureRegistry) {
        this.featureRegistry = featureRegistry;
        this.FEATURE_HANDLERS = Stream.of(
                ModuleHandler.class,
                BehaviourHandler.class,
                TaskHandler.class,
                LegacyNpcExtraHandler.class,
                NpcExtraHandler.class,
                ExtraMenuHandler.class,
                LaserSelectorHandler.class,
                ShipModeSelectorHandler.class,
                PetGearSelectorHandler.class,
                DrawableHandler.class,
                ReviveSelectorHandler.class
        ).map(api::requireInstance).collect(Collectors.toList());
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
