package com.github.manolo8.darkbot.extensions.plugins;

public interface PluginListener {
    default void beforeLoad(){}
    default void afterLoad(){}
    default void afterLoadComplete() {}
}
