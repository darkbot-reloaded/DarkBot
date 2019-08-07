package com.github.manolo8.darkbot.config.types.suppliers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ModuleSupplier extends OptionList<String> {

    public static List<String> MODULES = new ArrayList<>();

    @Override
    public String getValue(String text) {
        return text;
    }

    @Override
    public String getText(String value) {
        return value;
    }

    @Override
    public List<String> getOptions() {
        return MODULES;
    }

}
