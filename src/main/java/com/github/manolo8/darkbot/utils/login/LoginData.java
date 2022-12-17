package com.github.manolo8.darkbot.utils.login;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginData {
    private static final Pattern USER_ID_PATTERN = Pattern.compile("userID=(\\d+)");

    private int userId;
    private String username, password, sid, url, fullUrl, preloaderUrl, params;

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
        this.params = params;

        Matcher matcher = USER_ID_PATTERN.matcher(params);
        if (matcher.find()) {
            try {
                userId = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
                userId = 0;
            }
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
        return params;
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
