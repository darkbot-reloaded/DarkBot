package eu.darkbot.api.plugin;

import eu.darkbot.api.core.Tickable;

public interface Task extends Tickable {

    void onTickTask();

    @Override
    default void onTick() {
        this.onTickTask();
    }

    /**
     * This method is never called for {@link Task}
     */
    @Override
    default void onStopped() {
    }
}
