package com.github.manolo8.darkbot.extensions.plugins;

public interface PluginListener {
    /**
     * Called before plugins reload, used to unload previous plugins
     */
    default void beforeLoad(){}

    /**
     * Called right after plugins finished reloading, used to update the features
     */
    default void afterLoad(){}

    /**
     * Called after all internals are done updating, used for tasks that require the load to be fully finished
     */
    default void afterLoadComplete() {}

    /**
     * Called from swing event thread after updates, used for UI updates
     */
    default void afterLoadCompleteUI() {}
}
