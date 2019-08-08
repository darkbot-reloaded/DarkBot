package com.github.manolo8.darkbot.extensions.behaviours;

import com.github.manolo8.darkbot.Main;

public interface Behaviour<C> {

    /**
     * The method to install the behaviour.
     * @param main Main to install the behaviour for.
     * @param config The config provided by the bot for your behaviour.
     */
    void install(Main main, C config);
    void tick();

}
