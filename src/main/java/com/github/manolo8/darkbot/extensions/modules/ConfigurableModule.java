package com.github.manolo8.darkbot.extensions.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Module;

public interface ConfigurableModule<C> extends Module {

    default void install(Main main) {};
    void install(Main main, C config);

}
