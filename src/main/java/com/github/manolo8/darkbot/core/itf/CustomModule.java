package com.github.manolo8.darkbot.core.itf;

public interface CustomModule extends Module {

    default String name() {
        return getClass().getSimpleName();
    }

    default Object configuration() {
        return null;
    }

}
