package com.github.manolo8.darkbot.config;

public class PlayerInfo {
    public String username;
    public int userId = -1;

    public boolean isWhitelisted;

    public PlayerInfo() {
    }

    public PlayerInfo(String username) {
        this.username = username;
    }

    public PlayerInfo(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return username + "(" + userId +")";
    }
}
