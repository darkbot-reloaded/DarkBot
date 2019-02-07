package com.github.manolo8.darkbot.config.types.suppliers;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class ModuleSupplier implements Supplier<OptionList> {

    private static final List<String> MODULES = Arrays.asList("Collect", "Loot", "Loot & Collect");
    private static final OptionList<Integer> MODULE_OPTIONS = new OptionList<Integer>() {
        @Override
        public Integer getValue(String text) {
            return MODULES.indexOf(text);
        }

        @Override
        public String getText(Integer value) {
            return MODULES.get(value);
        }

        @Override
        public Collection<String> getOptions() {
            return MODULES;
        }
    };

    @Override
    public OptionList<Integer> get() {
        return MODULE_OPTIONS;
    }
}
