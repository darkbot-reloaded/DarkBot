package com.github.manolo8.darkbot.utils;

public class Time {
    
    public static String toString(long time) {
        StringBuilder builder = new StringBuilder();
        int seconds = (int)(time / 1000L);
        if (seconds >= 3600) {
            int hours = seconds / 3600;
            if (hours < 10) {
                builder.append('0');
            }
            builder.append(hours).append(':');
        }
        if (seconds >= 60) {
            int minutes = seconds % 3600 / 60;
            if (minutes < 10) {
                builder.append('0');
            }
            builder.append(minutes).append(':');
        }
        if ((seconds %= 60) < 10) {
            builder.append('0');
        }
        builder.append(seconds);
        return builder.toString();
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {}
    }
    
}
