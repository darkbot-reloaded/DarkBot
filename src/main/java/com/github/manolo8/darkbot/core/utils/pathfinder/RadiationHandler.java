package com.github.manolo8.darkbot.core.utils.pathfinder;

import com.github.manolo8.darkbot.Main;

public class RadiationHandler {
    private final Main main;
    private boolean changed;

    public RadiationHandler(Main main) {
        this.main = main;
        this.changed = main.config.MISCELLANEOUS.AVOID_RADIATION;
    }

    public boolean changed() {
        boolean avoid = main.config.MISCELLANEOUS.AVOID_RADIATION;

        if (changed != avoid) {
            changed = avoid;
            return true;
        }

        return false;
    }
}
