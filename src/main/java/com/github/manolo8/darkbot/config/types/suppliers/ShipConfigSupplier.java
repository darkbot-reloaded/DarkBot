package com.github.manolo8.darkbot.config.types.suppliers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ShipConfigSupplier implements Supplier<OptionList> {

    private static final List<String> options = Arrays.asList("1", "2");
    private static final OptionList<Integer> CONFIG_OPTIONS = new OptionList<Integer>() {
        @Override
        public Integer getValue(String text) {
            return Integer.parseInt(text);
        }

        @Override
        public String getText(Integer value) {
            return value.toString();
        }

        @Override
        public List<String> getOptions() {
            return options;
        }
    };

    @Override
    public OptionList<Integer> get() {
        return CONFIG_OPTIONS;
    }
}
