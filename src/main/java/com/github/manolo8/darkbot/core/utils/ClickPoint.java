package com.github.manolo8.darkbot.core.utils;

public class ClickPoint {
    public int x, y;
    public ClickPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public synchronized void set(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
