package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Module;

public abstract class TemporalModule implements Module {

    private Main main;
    private eu.darkbot.api.extensions.Module back;

    @Override
    public void install(Main main) {
        this.main = main;
        this.back = main.getModule();
        if (this.back instanceof TemporalModule)
            this.back = ((TemporalModule) this.back).back;
    }

    protected void goBack() {
        if (this.back instanceof eu.darkbot.api.extensions.TemporalModule) {
            ((eu.darkbot.api.extensions.TemporalModule) this.back).goBack();
        } else {
            main.setModule(this.back);
        }
        back = null;
    }
}
