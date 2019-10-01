package com.github.manolo8.darkbot.core;

import com.github.manolo8.darkbot.utils.Time;

public class DarkBotAPI extends AbstractDarkBotApi {

    static {
        System.loadLibrary("DarkBot");
    }

    public void createWindow() {
        new Thread(() -> {
            createWindow0();
            System.exit(0);
        }, "BotBrowser").start();
        new Thread(() -> {
            while ((window = USER_32.FindWindow("DarkBrowser", "DarkBrowser")) == null || !USER_32.IsWindow(window)) Time.sleep(100);
        }).start();
    }

    private native void createWindow0();

    public native boolean isValid();

    public native void mouseClick(int x, int y);

    public native void keyboardClick(char btn);

    public native double readMemoryDouble(long address);

    public native long readMemoryLong(long address);

    public native int readMemoryInt(long address);

    public native boolean readMemoryBoolean(long address);

    public native byte[] readMemory(long address, int length);

    public native void writeMemoryDouble(long address, double value);

    public native void writeMemoryLong(long address, long value);

    public native void writeMemoryInt(long address, int value);

    public native long[] queryMemoryInt(int value, int maxQuantity);

    public native long[] queryMemoryLong(long value, int maxQuantity);

    public native long[] queryMemory(byte[] query, int maxQuantity);

    public void handleRefresh() {
        USER_32.SetForegroundWindow(window);
        refresh();
    }

    public native void refresh();

}
