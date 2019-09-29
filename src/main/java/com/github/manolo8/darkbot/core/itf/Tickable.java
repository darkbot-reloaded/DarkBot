package com.github.manolo8.darkbot.core.itf;

public interface Tickable {
    void tick();
    default void tickStopped() {}
}
