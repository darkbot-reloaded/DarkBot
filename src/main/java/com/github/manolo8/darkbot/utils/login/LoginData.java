package com.github.manolo8.darkbot.utils.login;

import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.google.gson.reflect.TypeToken;
import eu.darkbot.api.managers.ConfigAPI;
import lombok.Getter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class LoginData {
    private static final Type PARAMS_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    private transient BiConsumer<String, String> onSetSid;

    private Map<String, String> params;
    @Getter
    private int userId;
    @Getter
    private String username, password, sid, url, fullUrl, preloaderUrl;

    public void setCredentials(String username, String password, BiConsumer<String, String> onSetSid) {
        this.username = username;
        this.password = password;
        this.onSetSid = onSetSid;
    }

    public void setSid(String sid, String url) {
        this.sid = sid;
        this.url = url;
        this.fullUrl = url == null ? null : "https://" + url + "/";

        if (onSetSid != null) onSetSid.accept(sid, url);
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

    public String getParams(ConfigAPI config) {
        if (isNotInitialized()) return null;
        // create copy of params to keep original values
        return FlashVarReplacement.createVarsString(config, new HashMap<>(params));
    }

    public boolean isNotInitialized() {
        return preloaderUrl == null || params == null;
    }

    public void reset() {
        setCredentials(null, null, null);
        setSid(null, null);
        userId = 0;
        preloaderUrl = null;
        params = null;
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
