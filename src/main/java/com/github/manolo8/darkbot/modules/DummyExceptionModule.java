package com.github.manolo8.darkbot.modules;

public class DummyExceptionModule extends DummyModule {

    private final String feature;

    public DummyExceptionModule(String feature) {
        this.feature = feature;
    }

    @Override
    public String getStatus() {
        return "Module '" + feature + "' failed with critical error!\nSee plugins tab for more info";
    }

    @Override
    public String getStoppedStatus() {
        return getStatus();
    }
}
