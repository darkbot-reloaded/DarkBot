package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.config.ConfigEntity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

public class I18n {

    private static Properties props = new Properties();
    static {
        reloadProps();
    }
    public static void reloadProps() {
        props.clear();
        Locale lang = ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.lang;
        loadProp(props, "/translations.properties");
        if (!lang.toLanguageTag().equals("en"))
            loadProp(props, "/translations_" + lang.toLanguageTag() + ".properties");
    }
    private static void loadProp(Properties props, String file) {
        URL resource = I18n.class.getResource(file);
        if (resource == null) {
            System.out.println("Couldn't find " + file + ", using defaults only");
            return;
        }
        try {
            props.load(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.print("Failed to load translations: " + resource);
            e.printStackTrace();
        }
    }

    private I18n() {}

    public static String getOrDefault(String key, String fallback) {
        if (key == null) return fallback;
        String res = (String) props.get(key);
        return res != null ? res : fallback;
    }

}
