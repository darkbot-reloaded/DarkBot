package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.config.ConfigEntity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class I18n {

    private static final Object[] EMPTY = {};
    public static final List<Locale> SUPPORTED_LOCALES = Stream.of(
            "bg", "cs", "de", "el", "en", "es", "fr", "hu", "it", "lt", "pl", "pt", "ro", "ru", "sv", "tr", "uk"
    ).map(Locale::new).sorted(Comparator.comparing(Locale::getDisplayName)).collect(Collectors.toList());
    private static final Properties props = new Properties();
    private static Locale locale;
    static {
        reloadProps();
    }

    public static void reloadProps() {
        props.clear();
        locale = ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.BOT_GUI.LOCALE;

        loadResource(props, getLangFile(Locale.ENGLISH));
        if (!locale.equals(Locale.ENGLISH)) loadResource(props, getLangFile(locale));
    }

    public static Locale getLocale() {
        return locale;
    }

    private static URL getLangFile(Locale locale) {
        URL res = I18n.class.getResource("/lang/strings_" + locale.toLanguageTag() + ".properties");
        if (res == null) System.out.println("Couldn't find translation file for " + locale);
        return res;
    }

    public static void loadResource(Properties props, URL resource) {
        if (resource == null) return;
        try {
            props.load(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Failed to load translations: " + resource);
            e.printStackTrace();
        }
    }

    private I18n() {}

    private static String getInternal(String key) {
        if (key == null) throw new IllegalArgumentException("Translation key must not be null");
        String res = (String) props.get(key);
        return res != null ? res : "Missing " + key;
    }

    private static String getOrDefaultInternal(String key, String fallback) {
        if (key == null) return fallback;
        String res = (String) props.get(key);
        return res != null ? res : fallback;
    }

    public static String get(String key) {
        return MessageFormat.format(getInternal(key), EMPTY);
    }

    public static String get(String key, Object... arguments) {
        return MessageFormat.format(getInternal(key), arguments);
    }

    public static String getOrDefault(String key, String fallback) {
        String text = getOrDefaultInternal(key, fallback);
        if (text == null) return null;
        return MessageFormat.format(text, EMPTY);
    }

    public static String getOrDefault(String key, String fallback, Object... arguments) {
        String text = getOrDefaultInternal(key, fallback);
        if (text == null) return null;
        return MessageFormat.format(text, arguments);
    }

}
