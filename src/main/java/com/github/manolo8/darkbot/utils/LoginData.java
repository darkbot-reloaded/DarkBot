package com.github.manolo8.darkbot.utils;

public class LoginData {
    private String username, password, sid, url, preloaderUrl, params;

    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setSid(String sid, String url) {
        this.sid = sid;
        this.url = url;
    }

    public void setPreloader(String preloaderUrl, String params) {
        this.preloaderUrl = preloaderUrl;
        this.params = params;
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

    public String getPreloaderUrl() {
        return preloaderUrl;
    }

    public String getParams() {
        return params;
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
