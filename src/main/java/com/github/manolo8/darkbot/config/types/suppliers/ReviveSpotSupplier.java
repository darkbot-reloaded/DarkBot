package com.github.manolo8.darkbot.config.types.suppliers;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class ReviveSpotSupplier extends OptionList<Long> {

    private static final List<String> LOCATIONS = Arrays.asList("Base", "Portal", "Spot");

    @Override
    public Long getValue(String text) {
        return (long) (LOCATIONS.indexOf(text) + 1);
    }

    @Override
    public String getText(Long value) {
        return LOCATIONS.get((int) (value - 1));
    }

    @Override
    public List<String> getOptions() {
        return LOCATIONS;
    }

}
