package com.github.manolo8.darkbot.core.utils.pathfinder;

import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.managers.ConfigAPI;

import java.util.function.Consumer;

public class RadiationHandler {
    private final Consumer<Boolean> listener = b -> this.changed = true;
    private boolean changed;

    public RadiationHandler(ConfigAPI configAPI) {
        ConfigSetting<Boolean> avoidRadiation = configAPI.requireConfig("miscellaneous.avoid_radiation");
        avoidRadiation.addListener(listener);
    }

    public boolean changed() {
        boolean result = this.changed;
        this.changed = false;
        return result;
    }
}
