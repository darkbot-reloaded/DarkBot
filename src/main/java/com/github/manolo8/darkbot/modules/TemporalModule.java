package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Module;

public abstract class TemporalModule implements Module {

    private Main main;
    private Module back;

    @Override
    public void install(Main main) {
        this.main = main;
        this.back = main.module;
    }

    protected void goBack() {
        if (back != null) main.setModule(this.back);
        back = null;
    }

}
