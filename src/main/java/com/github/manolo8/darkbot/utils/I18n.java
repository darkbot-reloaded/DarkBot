package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.config.ConfigEntity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class I18n {

    public static final List<Locale> SUPPORTED_LOCALES = Stream.of("en", "hu", "cs", "pl").map(Locale::new).collect(Collectors.toList());
    private static final Properties props = new Properties();
    static {
        reloadProps();
    }

    public static void reloadProps() {
        props.clear();
        Locale locale = ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.lang;

        loadResource(props, getLangFile(Locale.ENGLISH));
        if (!locale.equals(Locale.ENGLISH)) loadResource(props, getLangFile(locale));
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

    public static String getOrDefault(String key, String fallback) {
        if (ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.DEV_STUFF) {
            System.out.println("Getting translation for " + Objects.toString(key, "null") +
                    (key == null ? "" : ": " + Objects.toString(props.get(key), "null")));
        }
        if (key == null) return fallback;
        String res = (String) props.get(key);
        return res != null ? res : fallback;
    }

}
