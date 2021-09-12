package com.github.manolo8.darkbot.core;

import com.github.manolo8.darkbot.utils.LibUtils;

public class DarkBotAPI {

    static {
        System.load(LibUtils.getLibPath("DarkBot"));
    }

    public native void createWindow0();

    public native boolean isValid();

    public native void mouseMove(int x, int y);

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

    public native void refresh();

}
