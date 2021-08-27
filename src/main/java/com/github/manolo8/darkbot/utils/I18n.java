package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.config.ConfigEntity;
import eu.darkbot.api.managers.I18nAPI;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Prefer to use I18nAPI instance instead
 */
public class I18n {

    // Hack for backwards compat
    private static eu.darkbot.impl.managers.I18n INSTANCE;

    public static final List<Locale> SUPPORTED_LOCALES = Stream.of(
            "bg", "cs", "de", "el", "en", "es", "fr", "hu", "it", "lt", "pl", "pt", "ro", "ru", "sv", "tr", "uk"
    ).map(Locale::new).sorted(Comparator.comparing(Locale::getDisplayName)).collect(Collectors.toList());

    public static void init(eu.darkbot.impl.managers.I18n instance) {
        INSTANCE = instance;
    }

    public static I18nAPI getInstance() {
        return INSTANCE;
    }

    private I18n() {}

    public static void updateLang() {
        INSTANCE.setLocale(ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.BOT_GUI.LOCALE);
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

}
