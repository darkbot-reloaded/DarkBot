package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.gui.utils.Strings;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class LanguageSupplier extends OptionList<Locale> {

    private static final List<Locale> LOCALES = Arrays.asList(Locale.ENGLISH, Locale.forLanguageTag("hu"));
    private static final List<String> LOCALE_NAMES = LOCALES.stream()
            .map(Locale::getDisplayName).map(Strings::capitalize).collect(Collectors.toList());

    @Override
    public Locale getValue(String text) {
        return LOCALES.get(LOCALE_NAMES.indexOf(text));
    }

    @Override
    public String getText(Locale value) {
        return Strings.capitalize(value.getDisplayName());
    }

    @Override
    public List<String> getOptions() {
        return LOCALE_NAMES;
    }

}
