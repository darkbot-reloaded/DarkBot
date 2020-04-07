package com.github.manolo8.darkbot.core;

public interface IDarkBotAPI {

    void createWindow();

    boolean isValid();

    void mouseMove(int x, int y);

    void mouseClick(int x, int y);

    void keyboardClick(char btn);

    void keyboardClick(Character ch);

    void sendText(String string);

    double readMemoryDouble(long address);

    default double readMemoryDouble(long address, int... offsets) {
        for (int offset = 0; offset < offsets.length - 1; offset++) address = readMemoryLong(address + offset);
        return readMemoryDouble(address + offsets[offsets.length - 1]);
    }

    long readMemoryLong(long address);

    default long readMemoryLong(long address, int... offsets) {
        for (int offset : offsets) address = readMemoryLong(address + offset);
        return address;
    }

    int readMemoryInt(long address);

    default int readMemoryInt(long address, int... offsets) {
        for (int offset = 0; offset < offsets.length - 1; offset++) address = readMemoryLong(address + offset);
        return readMemoryInt(address + offsets[offsets.length - 1]);
    }

    boolean readMemoryBoolean(long address);

    default boolean readMemoryBoolean(long address, int... offsets) {
        for (int offset = 0; offset < offsets.length - 1; offset++) address = readMemoryLong(address + offset);
        return readMemoryBoolean(address + offsets[offsets.length - 1]);
    }

    String readMemoryString(long address);

    default String readMemoryString(long address, int... offsets) {
        for (int offset : offsets) address = readMemoryLong(address + offset);
        return readMemoryString(address);
    }

    byte[] readMemory(long address, int length);

    void writeMemoryDouble(long address, double value);

    void writeMemoryLong(long address, long value);

    void writeMemoryInt(long address, int value);

    long[] queryMemoryInt(int value, int maxQuantity);

    long[] queryMemoryLong(long value, int maxQuantity);

    long[] queryMemory(byte[] query, int maxQuantity);

    void setVisible(boolean visible);

    void setRender(boolean visible);

    void handleRefresh();

    void refresh();

    static LoggingAPIHandler getLoggingHandler(DarkBotAPI API) {
        return new LoggingAPIHandler(API);
    }
}
