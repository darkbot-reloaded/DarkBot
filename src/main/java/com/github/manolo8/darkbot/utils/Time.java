package com.github.manolo8.darkbot.utils;

public class Time {
    public static final int SECOND = 1000, MINUTE = SECOND * 60, HOUR = MINUTE * 60, DAY = HOUR * 24, WEEK = DAY * 7;

    public static String toString(Integer time) {
        if (time == null) return "-";
        return millisToString(time.longValue());
    }

    public static String toString(long time) {
        return millisToString(time);
    }

    public static String millisToString(long millis) {
        return secondsToString((int) (millis / 1000L));
    }

    public static String secondsToString(int seconds) {
        StringBuilder builder = new StringBuilder();
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

    /**
     * Seconds to short-string value
     */
    public static String secondsToShort(double time) {
        if (time < 60) return Math.round(time) + "s";
        if ((time /= 60) < 60) return Math.round(time) + "m";
        if ((time /= 60) < 60) return Math.round(time) + "h";
        if ((time /= 24) < 100) return Math.round(time) + "d";
        if ((time /= 7) < 100) return Math.round(time) + "w";
        if ((time = time * 7 / 365) < 100) return Math.round(time) + "y";
        return "∞ "; // Over 99 years
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
