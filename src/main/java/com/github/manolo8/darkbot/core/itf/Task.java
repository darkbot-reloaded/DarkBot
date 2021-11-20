package com.github.manolo8.darkbot.core.itf;

public interface Task extends Installable, Tickable, eu.darkbot.api.extensions.Task {
    default void tickTask() {
        tick();
    }

    default void backgroundTick() {}

    @Override
    default void onTickTask() {
        tickTask();
    }

    @Override
    default void onBackgroundTick() {
        backgroundTick();
    }
}
