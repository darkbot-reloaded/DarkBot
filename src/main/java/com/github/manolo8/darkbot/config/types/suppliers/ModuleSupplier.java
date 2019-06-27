package com.github.manolo8.darkbot.config.types.suppliers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ModuleSupplier extends OptionList<Integer> {

    private static final List<String> MODULES = Arrays.asList("Collector", "Npc Killer", "Kill & Collect", "Experiment zones", "Custom");

    @Override
    public Integer getValue(String text) {
        return MODULES.indexOf(text);
    }

    @Override
    public String getText(Integer value) {
        return MODULES.get(value);
    }

    @Override
    public List<String> getOptions() {
        return MODULES;
    }

}
