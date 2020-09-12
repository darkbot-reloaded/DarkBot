package com.github.manolo8.darkbot.backpage.hangar;

public class Money {
    private String uridium, credits;

    public String getUridium() {
        return uridium;
    }

    public String getCredits() {
        return credits;
    }

    @Override
    public String toString() {
        return "Money{" +
                "uridium='" + uridium + '\'' +
                ", credits='" + credits + '\'' +
                '}';
    }
}
