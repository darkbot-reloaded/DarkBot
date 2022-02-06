package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.behaviours.AutoRefine;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import eu.darkbot.api.extensions.Behavior;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BehaviourHandler extends FeatureHandler<Behavior> {

    private static final Class<?>[] NATIVE = new Class[]{AutoRefine.class};

    private final Main main;
    private final FeatureRegistry featureRegistry;

    public BehaviourHandler(Main main, FeatureRegistry featureRegistry) {
        this.main = main;
        this.featureRegistry = featureRegistry;
    }

    @Override
    public Class<?>[] getNativeFeatures() {
        return NATIVE;
    }

    @Override
    public void update(Stream<FeatureDefinition<Behavior>> behaviours) {
        main.setBehaviours(behaviours
                .map(featureRegistry::getFeature)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));
    }

}
