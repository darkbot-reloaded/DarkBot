package com.github.manolo8.darkbot.core.def;

import com.github.manolo8.darkbot.Main;

public abstract class Module {

    protected Main main;

    public Module(Main main) {
        this.main = main;
    }

    public abstract void install();

    public abstract void tick();

}
