package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.config.ConfigEntity;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class I18n {

    private static String lang = ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.lang;
    private static Properties props = new Properties();
    static {
        load(props,I18n.class.getResource("/translations.properties"));
        if (!lang.equals("en")) {
            load(props,I18n.class.getResource("/translations_" + lang + ".properties"));
        }
    }
    private static void load(Properties props, URL resource) {
        try {
            props.load(resource.openStream());
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
