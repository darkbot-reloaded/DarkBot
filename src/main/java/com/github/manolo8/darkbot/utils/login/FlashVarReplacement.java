package com.github.manolo8.darkbot.utils.login;

import com.github.manolo8.darkbot.utils.I18n;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.managers.ConfigAPI;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum FlashVarReplacement {
    AUTO_START("autoStartEnabled", c -> "1"),
    DISPLAY_TYPE("display2d", FlashVarReplacement::use3D),
    IN_GAME_LANGUAGE("lang", FlashVarReplacement::forceLanguage);

    private static final FlashVarReplacement[] VALUES = values();

    private final String key;
    private final Function<ConfigAPI, String> valueFunction;

    FlashVarReplacement(String key, Function<ConfigAPI, String> valueFunction) {
        this.key = key;
        this.valueFunction = valueFunction;
    }

    public static String createVarsString(ConfigAPI config, Map<String, String> params) {
        return applyAll(config, params).entrySet().stream()
                .map(Object::toString)
                .collect(Collectors.joining("&"));
    }

    private static Map<String, String> applyAll(ConfigAPI config, Map<String, String> values) {
        for (FlashVarReplacement r : VALUES) {
            String replacement = r.valueFunction.apply(config);
            if (replacement != null) {
                values.put(r.key, replacement);
            }
        }
        return values;
    }

    private static String use3D(ConfigAPI c) {
        ConfigSetting<Boolean> use3D = c.requireConfig("bot_settings.api_config.use_3d");
        return use3D.getValue() ? "1" : "2";
    }

    private static String forceLanguage(ConfigAPI c) {
        String language = I18n.getLocale().getLanguage();
        ConfigSetting<Boolean> forceLanguage = c.requireConfig("bot_settings.api_config.force_game_language");
        return forceLanguage.getValue() && !language.isEmpty() ? language : null;
    }
}