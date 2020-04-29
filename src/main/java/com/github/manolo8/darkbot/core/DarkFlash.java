package com.github.manolo8.darkbot.core;

public class DarkFlash {

    static {
        System.loadLibrary("lib/DarkFlash");
    }

    public native void setCookie(String url, String cookie);

    public native void loadSWF(String preloader, String params, String url);

    public native void reloadSWF();

    public native void mousePress(int x, int y);

    public native void keyPress(char btn);

    public native double readMemoryDouble(long address);

    public native long readMemoryLong(long address);

    public native int readMemoryInt(long address);

    public native byte[] readMemory(long address, int length);

    public native void writeMemoryDouble(long address, double value);

    public native void writeMemoryLong(long address, long value);

    public native void writeMemoryInt(long address, int value);

    public native long[] queryMemoryInt(int value, int maxQuantity);

    public native long[] queryMemoryLong(long value, int maxQuantity);

    public native long[] queryMemory(byte[] query, int maxQuantity);

    public native void setVisible(boolean flag);

    public native void setRender(boolean flag);

}
