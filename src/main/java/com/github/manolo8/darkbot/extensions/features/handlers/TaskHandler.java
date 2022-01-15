package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.FlashResManager;
import com.github.manolo8.darkbot.backpage.UsernameUpdater;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import eu.darkbot.api.extensions.Task;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskHandler extends FeatureHandler<Task> {

    private static final Class<?>[] NATIVE = new Class[]{UsernameUpdater.class, FlashResManager.class}; //todo

    private final Main main;

    public TaskHandler(Main main) {
        this.main = main;
    }

    @Override
    public Class<?>[] getNativeFeatures() {
        return NATIVE;
    }

    @Override
    public void update(Stream<FeatureDefinition<Task>> tasks) {
        main.backpage.setTasks(tasks
                .map(featureRegistry::getFeature)
                .map(o -> o.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

}
