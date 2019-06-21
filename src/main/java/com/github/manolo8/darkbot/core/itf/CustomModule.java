package com.github.manolo8.darkbot.core.itf;

import com.github.manolo8.darkbot.Main;

public interface CustomModule<C> extends Module {

    /**
     * @return Display name of the module in the config tree.
     */
    default String name() {
        return getClass().getSimpleName();
    }

    /**
     * @return Name of the author or authors.
     */
    default String author() {
        return "no_author";
    }

    /**
     * @return String that uniquely identifies this module. Used to store configurations under this key.
     */
    default String id() {
        return name() + "_" + author();
    }

    /**
     * @return The class used for configuration. Null if the module doesn't use configuration.
     */
    default Class<C> configuration() {
        return null;
    }

    /**
     * Custom modules should avoid using the plain install method from Module, and use install with config instead.
     */
    default void install(Main main) {}

    /**
     * The method to install the main in your module.
     * @param main Main to install the module for.
     * @param config The config provided by the bot for your module.
     */
    void install(Main main, C config);

}
