package com.github.manolo8.darkbot.core.itf;

public interface Task extends Installable, Tickable {
    default void tickTask() {
        tick();
    }
}
