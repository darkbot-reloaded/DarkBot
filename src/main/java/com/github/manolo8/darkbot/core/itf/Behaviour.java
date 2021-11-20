package com.github.manolo8.darkbot.core.itf;

import eu.darkbot.api.extensions.Behavior;

public interface Behaviour extends Installable, Tickable, Behavior {
    default void tickBehaviour() {
        tick();
    }

    @Override
    default void onTickBehavior() {
        this.tickBehaviour();
    }

    @Override
    default void onStoppedBehavior() {
        this.tickStopped();
    }
}
