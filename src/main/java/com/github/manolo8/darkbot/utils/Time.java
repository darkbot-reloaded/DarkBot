package com.github.manolo8.darkbot.utils;

public class Time {
    public static final int SECOND = 1000, MINUTE = SECOND * 60, HOUR = MINUTE * 60, DAY = HOUR * 24, WEEK = DAY * 7;

    public static String toString(Integer time) {
        if (time == null) return "-";
        return toString(time.intValue());
    }

    public static String toString(long time) {
        StringBuilder builder = new StringBuilder();
        int seconds = (int) (time / 1000L);
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

    public static void sleepMax(long time, int total) {
        time = System.currentTimeMillis() - time;
        sleep(total - time);
    }

    public static void sleep(long millis) {
        if (millis <= 0) return;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {}
    }

}
