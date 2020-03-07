package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.gui.utils.Strings;
import com.github.manolo8.darkbot.utils.I18n;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LanguageSupplier extends OptionList<Locale> {

    private static final List<Locale> LOCALES = I18n.SUPPORTED_LOCALES;
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
