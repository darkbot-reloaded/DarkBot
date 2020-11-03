package eu.darkbot.api.plugin;

import eu.darkbot.api.core.Tickable;

public interface Behavior extends Tickable {

    void onTickBehavior();

    void onStoppedBehavior();

    @Override
    default void onTick() {
        this.onTickBehavior();
    }

    @Override
    default void onStopped() {
        this.onStoppedBehavior();
    }
}
