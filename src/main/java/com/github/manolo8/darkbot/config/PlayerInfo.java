package com.github.manolo8.darkbot.config;

public class PlayerInfo {
    public String username;
    public int userId = -1;

    public int retries = 0;
    public long lastUpdate = 0;

    public boolean isWhitelisted;

    public PlayerInfo() {
    }

    public PlayerInfo(String username) {
        this.username = username;
    }

    public PlayerInfo(int userId) {
        this.userId = userId;
    }

    public boolean shouldWait() {
        return System.currentTimeMillis() > lastUpdate + (retries * retries * 1000);
    }

    @Override
    public String toString() {
        return username + "(" + userId +")";
    }
}
