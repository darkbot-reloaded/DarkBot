package com.github.manolo8.darkbot.utils.login;

import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.utils.I18n;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class LoginData {
    private static final Type PARAMS_TYPE = new TypeToken<Map<String, String>>() {}.getType();
    private static final Map<String, BiFunction<Config, String, String>> REPLACEMENTS = new HashMap<>() {{
        put("display2d", (config, defValue) -> config.BOT_SETTINGS.API_CONFIG.USE_3D ? "1" : "2");
        put("autoStartEnabled", (config, defValue) -> "1");
        put("lang", (config, defValue) -> Optional.of(I18n.getLocale().getLanguage())
                .filter(l -> !l.isEmpty())
                .filter(l -> config.BOT_SETTINGS.API_CONFIG.FORCE_GAME_LANGUAGE)
                .orElse(defValue));
    }};

    private int userId;
    private Map<String, String> params;
    private String username, password, sid, url, fullUrl, preloaderUrl;

    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setSid(String sid, String url) {
        this.sid = sid;
        this.url = url;
        this.fullUrl = "https://" + url + "/";
    }

    public void setPreloader(String preloaderUrl, String params) {
        this.preloaderUrl = preloaderUrl;
        this.params = BackpageManager.GSON.fromJson(params, PARAMS_TYPE);
        try {
            userId = Integer.parseInt(this.params.get("userID"));
        } catch (NumberFormatException ignored) {
            userId = 0;
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSid() {
        return sid;
    }

    public String getUrl() {
        return url;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public String getPreloaderUrl() {
        return preloaderUrl;
    }

    public String getParams() {
        return params.entrySet().stream()
                .map(this::mapEntry)
                .collect(Collectors.joining("&"));
    }

    public int getUserId() {
        return userId;
    }

    private String mapEntry(Map.Entry<String, String> e) {
        BiFunction<Config, String, String> func = REPLACEMENTS.get(e.getKey());
        String value = func == null ? e.getValue() : func.apply(ConfigEntity.INSTANCE.getConfig(), e.getValue());
        return e.getKey() + "=" + value;
    }

    @Override
    public String toString() {
        return "LoginData{" +
                "sid='" + sid + '\'' +
                ", url='" + url + '\'' +
                ", preloaderUrl='" + preloaderUrl + '\'' +
                ", params='" + params + '\'' +
                '}';
    }
}
