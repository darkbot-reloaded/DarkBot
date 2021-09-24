package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.utils.I18n;

import java.util.Arrays;
import java.util.List;

@Deprecated
public class ReviveSpotSupplier extends OptionList<Long> {

    private static final List<String> LOCATIONS = Arrays.asList(
            I18n.get("config.general.safety.revive_location.list.base"),
            I18n.get("config.general.safety.revive_location.list.portal"),
            I18n.get("config.general.safety.revive_location.list.spot"));

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
