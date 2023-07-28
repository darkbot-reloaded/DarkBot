package com.github.manolo8.darkbot.utils.login;

import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.google.gson.reflect.TypeToken;
import eu.darkbot.api.managers.ConfigAPI;

import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

public class LoginData {
    private static final Type PARAMS_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    private int userId;
    private Map<String, String> params;
    private String username, password, sid, url, fullUrl, preloaderUrl;

    private transient Credentials credentials;
    private transient char[] masterPass;

    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setCredentials(Credentials credentials, char[] masterPass) {
        this.credentials = credentials;
        this.masterPass = masterPass;
    }

    public void setSid(String sid, String url) {
        this.sid = sid;
        this.url = url;
        this.fullUrl = "https://" + url + "/";

        if (username != null && credentials != null) {
            credentials.getUsers().stream()
                    .filter(user -> username.equals(user.u))
                    .filter(user -> !sid.equals(user.s))
                    .findFirst()
                    .ifPresent(user -> {
                        user.setSid(sid, url);
                        try {
                            LoginUtils.saveCredentials(credentials, masterPass);
                        } catch (GeneralSecurityException e) {
                            System.err.println("Failed to write sid & server to credentials file");
                        }
                    });
        }
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
