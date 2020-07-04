package com.github.manolo8.darkbot.config;

/**
 * Queued player waiting for ID or Username to be resolved by a background runner
 */
public class UnresolvedPlayer {
    public String username;
    public int userId = -1;

    public int retries = 0;
    public long lastUpdate = 0;

    public UnresolvedPlayer() {}

    public UnresolvedPlayer(String username) {
        this.username = username;
    }

    public UnresolvedPlayer(int userId) {
        this.userId = userId;
    }

    public boolean shouldWait() {
        return System.currentTimeMillis() < lastUpdate + (retries * retries * 1000);
    }

}
