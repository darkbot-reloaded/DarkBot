package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Module;

/**
 * @deprecated Use {@link eu.darkbot.shared.modules.TemporalModule}
 */
@Deprecated(forRemoval = true)
public abstract class TemporalModule implements Module, eu.darkbot.api.extensions.TemporalModule {

    private Main main;
    private eu.darkbot.api.extensions.Module back;

    @Override
    public void install(Main main) {
        this.main = main;
        this.back = main.getNonTemporalModule();
    }

    @Override
    public eu.darkbot.api.extensions.Module getBack() {
        return back;
    }

    public void goBack() {
        main.setModule(this.back);
        back = null;
    }
}
