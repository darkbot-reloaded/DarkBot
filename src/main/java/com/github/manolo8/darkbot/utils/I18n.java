package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.gui.utils.Popups;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.I18nAPI;

import javax.swing.*;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Prefer to use I18nAPI instance instead
 */
public class I18n {

    // Hack for backwards compat
    private static eu.darkbot.impl.managers.I18n INSTANCE;
    private static final LangChangeListener onLanguageChange = new LangChangeListener();

    public static final List<Locale> SUPPORTED_LOCALES = Stream.of(
            "bg", "cs", "de", "el", "en", "es", "fr", "hu", "it", "lt", "pl", "pt", "ro", "ru", "sv", "tr", "uk"
    ).map(Locale::new).sorted(Comparator.comparing(Locale::getDisplayName)).collect(Collectors.toList());

    public static void init(PluginAPI api, Locale locale) {
        INSTANCE = api.requireInstance(eu.darkbot.impl.managers.I18n.class);
        INSTANCE.setLocale(locale);

        ConfigSetting<Locale> setting = api.requireAPI(ConfigAPI.class).requireConfig("bot_settings.bot_gui.locale");
        setting.addListener(onLanguageChange);
    }

    public static I18nAPI getInstance() {
        return INSTANCE;
    }

    private I18n() {}

    public static boolean setLocale(Locale locale) {
        if (Objects.equals(I18n.getLocale(), locale)) return false;
        INSTANCE.setLocale(locale);
        return true;
    }

    public static Locale getLocale() {
        return INSTANCE.getLocale();
    }

    public static String get(String key) {
        return INSTANCE.get(key);
    }

    public static String get(String key, Object... arguments) {
        return INSTANCE.get(key, arguments);
    }

    public static String getOrDefault(String key, String fallback) {
        return INSTANCE.getOrDefault(key, fallback);
    }

    public static String getOrDefault(String key, String fallback, Object... arguments) {
        return INSTANCE.getOrDefault(key, fallback, arguments);
    }

    public static class LangChangeListener implements Consumer<Locale> {
        @Override
        public void accept(Locale loc) {
            if (I18n.setLocale(loc)) {
                Popups.of(
                                I18n.get("language.changed.title"),
                                I18n.get("language.changed.content", loc.getDisplayName(loc),
                                        I18n.get("translation.credit")), JOptionPane.INFORMATION_MESSAGE)
                        .showAsync();
            }
        }
    }

}
