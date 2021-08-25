package com.github.manolo8.darkbot.config.tree.handlers;


import com.github.manolo8.darkbot.config.tree.ConfigSetting;

public interface ValueHandler<T> {

    T validate(T t);

    void update(ConfigSetting<T> setting, T t);

}
