package com.github.manolo8.darkbot.utils.login;

import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.google.gson.reflect.TypeToken;
import eu.darkbot.api.managers.ConfigAPI;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class LoginData {
    private static final Type PARAMS_TYPE = new TypeToken<Map<String, String>>() {}.getType();

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

    public String getParams(ConfigAPI config) {
        if (isNotInitialized()) return null;
        // create copy of params to keep original values
        return FlashVarReplacement.createVarsString(config, new HashMap<>(params));
    }

    public boolean isNotInitialized() {
        return preloaderUrl == null || params == null;
    }

    public int getUserId() {
        return userId;
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
