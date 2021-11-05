package com.github.manolo8.darkbot.core;

public interface IDarkBotAPI {

    void createWindow();
    void setSize(int width, int height);

    boolean isValid();
    boolean isInitiallyShown();
    long getMemoryUsage();
    String getVersion();

    void mouseMove(int x, int y);
    void mouseClick(int x, int y);

    @Deprecated
    default void keyboardClick(char btn) {
        rawKeyboardClick(Character.toUpperCase(btn));
    }

    void rawKeyboardClick(char btn);

    default void keyboardClick(Character ch) {
        if (ch != null) rawKeyboardClick(ch);
    }

    void sendText(String string);

    double readMemoryDouble(long address);
    default double readMemoryDouble(long address, int... offsets) {
        for (int i = 0; i < offsets.length - 1; i++) address = readMemoryLong(address + offsets[i]);
        return readMemoryDouble(address + offsets[offsets.length - 1]);
    }

    long readMemoryLong(long address);
    default long readMemoryLong(long address, int... offsets) {
        for (int offset : offsets) address = readMemoryLong(address + offset);
        return address;
    }

    int readMemoryInt(long address);
    default int readMemoryInt(long address, int... offsets) {
        for (int i = 0; i < offsets.length - 1; i++) address = readMemoryLong(address + offsets[i]);
        return readMemoryInt(address + offsets[offsets.length - 1]);
    }

    boolean readMemoryBoolean(long address);
    default boolean readMemoryBoolean(long address, int... offsets) {
        for (int i = 0; i < offsets.length - 1; i++) address = readMemoryLong(address + offsets[i]);
        return readMemoryBoolean(address + offsets[offsets.length - 1]);
    }

    String readMemoryString(long address);
    String readMemoryStringFallback(long address, String fallback);
    default String readMemoryString(long address, int... offsets) {
        for (int offset : offsets) address = readMemoryLong(address + offset);
        return readMemoryString(address);
    }
    default String readMemoryStringFallback(long address, String fallback, int... offsets) {
        for (int offset : offsets) address = readMemoryLong(address + offset);
        return readMemoryStringFallback(address, fallback);
    }

    byte[] readMemory(long address, int length);
    default void readMemory(long address, byte[] buffer) {
        readMemory(address, buffer, buffer.length);
    }
    void readMemory(long address, byte[] buffer, int length);

    void writeMemoryInt(long address, int value);
    void writeMemoryLong(long address, long value);
    void writeMemoryDouble(long address, double value);

    default void replaceInt(long address, int oldValue, int newValue) {
        writeMemoryInt(address, oldValue);
    }

    long[] queryMemoryInt(int value, int maxQuantity);
    long[] queryMemoryLong(long value, int maxQuantity);
    long[] queryMemory(byte[] query, int maxQuantity);

    void setVisible(boolean visible);
    default void setMinimized(boolean visible) {
        setVisible(false);
    }

    void handleRefresh();
    void resetCache();

}
