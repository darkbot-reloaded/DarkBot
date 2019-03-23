package com.github.manolo8.darkbot.core;

import java.nio.charset.StandardCharsets;

public class DarkBotAPI implements IDarkBotAPI {

    static {
        System.loadLibrary("DarkBot");
    }

    public void createWindow() {
        new Thread(() -> {
            createWindow0();
            System.exit(0);
        }, "BotBrowser").start();
    }

    private native void createWindow0();

    public native boolean isValid();

    public native void mousePress(int x, int y);

    public native void mouseMove(int x, int y);

    public native void mouseRelease(int x, int y);

    public native void mouseClick(int x, int y);

    public native void keyboardClick(char btn);

    public void keyboardClick(Character ch) {
        if (ch != null) keyboardClick((char) ch);
    }

    public native double readMemoryDouble(long address);

    public native long readMemoryLong(long address);

    public native int readMemoryInt(long address);

    public native boolean readMemoryBoolean(long address);

    public String readMemoryString(long address) {

        int flags = readMemoryInt(address + 36);
        int width = (flags & 0x00000001);
        int size = readMemoryInt(address + 32) << width;
        int type = (flags & 0x00000006) >> 1;

        if (size > 256 || size < 0) return "ERROR";

        byte[] bytes;

        if (type == 2)
            bytes = readMemory(readMemoryLong(readMemoryLong(address + 24) + 16) + readMemoryInt(address + 16), size);
        else
            bytes = readMemory(readMemoryLong(address + 16), size);

        return width == 0 ? new String(bytes, StandardCharsets.ISO_8859_1) : new String(bytes, StandardCharsets.UTF_16LE);
    }

    public native byte[] readMemory(long address, int length);

    public native void writeMemoryDouble(long address, double value);

    public native void writeMemoryLong(long address, long value);

    public native void writeMemoryInt(long address, int value);

    public native long[] queryMemoryInt(int value, int maxQuantity);

    public native long[] queryMemoryLong(long value, int maxQuantity);

    public native long[] queryMemory(byte[] query, int maxQuantity);

    public native void setVisible(boolean visible);

    public native void refresh();

}
