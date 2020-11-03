package eu.darkbot.api.plugin;

import eu.darkbot.api.core.Tickable;

public interface Module extends Tickable {

    void onTickModule();

    void onStoppedModule();

    @Override
    default void onTick() {
        this.onTickModule();
    }

    @Override
    default void onStopped() {
        this.onStoppedModule();
    }
}
