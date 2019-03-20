package com.github.manolo8.darkbot;

import java.net.HttpURLConnection;
import java.net.URL;

public class BackpageManager extends Thread {
    private final Main main;
    private static final int SECOND = 1000, MINUTE = 60 * SECOND;

    private String sid;
    private String instance;

    private volatile int sidStatus = -1;

    public BackpageManager(Main main) {
        super("BackpageManager");
        this.main = main;
        start();
    }

    @Override
    public void run() {
        int waitTime = 0;
        while (sidStatus != 302 && sleep((int) (waitTime + waitTime * Math.random()))) {
            if (isInvalid()) {
                sidStatus = -1;
                waitTime = SECOND;
                continue;
            }
            waitTime = validTick();
        }
    }

    private boolean isInvalid() {
        this.sid = main.statsManager.sid;
        this.instance = main.statsManager.instance;
        return sid == null || instance == null || sid.isEmpty() || instance.isEmpty();
    }

    private int validTick() {
        try {
            sidStatus = sidKeepAlive();
        } catch (Exception e) {
            sidStatus = -2;
            e.printStackTrace();
            return 5 * MINUTE;
        }
        return 10 * MINUTE;
    }

    private int sidKeepAlive() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(instance + "indexInternal.es").openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("Cookie", "dosid=" + sid);
        return conn.getResponseCode();
    }

    private boolean sleep(int millis) {
        if (millis <= 0) return true;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
        return true;
    }

    public String sidStatus() {
        switch (sidStatus) {
            case -1: return "--";
            case -2: return "ERR";
            case 200: return "OK";
            case 302: return "KO";
            default: return sidStatus + "";
        }
    }

}
