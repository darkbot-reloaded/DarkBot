package com.github.manolo8.darkbot.core.itf;

import com.github.manolo8.darkbot.Main;

public interface Module extends Installable, Tickable, RefreshHandler, eu.darkbot.api.extensions.Module {

    void install(Main main);

    boolean canRefresh();

    default void tickModule() {
        tick();
    }

    default String status() {
        return null;
    }

    default String stoppedStatus() {
        return null;
    }

    @Override
    default void onTickModule() {
        tickModule();
    }

    @Override
    default void onTickStopped() {
        tickStopped();
    }

    @Override
    default String getStatus() {
        return status();
    }

    @Override
    default String getStoppedStatus() {
        return stoppedStatus();
    }
}
