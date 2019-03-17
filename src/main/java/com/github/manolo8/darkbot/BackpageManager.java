package com.github.manolo8.darkbot;

public class BackpageManager extends Thread {
    private final Main main;
    private static final long MINUTE = 60 * 1000;

    private String sid;

    public BackpageManager(Main main) {
        super("BackpageManager");
        this.main = main;
        start();
    }

    @Override
    public void run() {
        while (true) {
            sleep((int) (10 * MINUTE + (Math.random() * 10 * MINUTE)));
            if (main.statsManager.sid == null) continue;

            //new URL("https://")
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

}
