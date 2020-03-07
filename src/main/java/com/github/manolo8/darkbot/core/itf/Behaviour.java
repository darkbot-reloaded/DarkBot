package com.github.manolo8.darkbot.core.itf;

public interface Behaviour extends Installable, Tickable {
    default void tickBehaviour() {
        tick();
    }
}
