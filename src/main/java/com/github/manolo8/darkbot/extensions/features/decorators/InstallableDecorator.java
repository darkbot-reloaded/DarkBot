package com.github.manolo8.darkbot.extensions.features.decorators;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Installable;

public class InstallableDecorator extends FeatureDecorator<Installable> {

    private final Main main;

    public InstallableDecorator(Main main) {
        this.main = main;
    }

    @Override
    protected void load(Installable obj) {
        obj.install(main);
    }

    @Override
    protected void unload(Installable obj) {
        obj.uninstall();
    }

}
