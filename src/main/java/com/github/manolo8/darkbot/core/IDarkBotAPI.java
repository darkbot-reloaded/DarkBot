package com.github.manolo8.darkbot.core;

import eu.darkbot.api.managers.MemoryAPI;
import eu.darkbot.api.managers.WindowAPI;

public interface IDarkBotAPI extends MemoryAPI, WindowAPI {

    void createWindow();
    void setSize(int width, int height);

    boolean isValid();
    long getMemoryUsage();
    int getVersion();

    void mouseMove(int x, int y);

    void mouseClick(int x, int y);

    void keyboardClick(char btn);
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

    void writeMemoryInt(long address, int value);
    void writeMemoryLong(long address, long value);
    void writeMemoryDouble(long address, double value);

    void replaceInt(long address, int oldValue, int newValue);

    long[] queryMemoryInt(int value, int maxQuantity);
    long[] queryMemoryLong(long value, int maxQuantity);
    long[] queryMemory(byte[] query, int maxQuantity);

    void setVisible(boolean visible);
    void setMinimized(boolean visible);

    void handleRefresh();
    void resetCache();

    //MemoryAPI
    @Override
    default int readInt(long address) {
        return readMemoryInt(address);
    }

    @Override
    default long readLong(long address) {
        return readMemoryLong(address);
    }

    @Override
    default double readDouble(long address) {
        return readMemoryDouble(address);
    }

    @Override
    default boolean readBoolean(long address) {
        return readMemoryBoolean(address);
    }

    @Override
    default String readString(long address) {
        return readMemoryString(address);
    }

    @Override
    default byte[] readBytes(long address, int length) {
        return readMemory(address, length);
    }

    @Override
    default void replaceLong(long address, long oldValue, long newValue) {
        if (readLong(address) == oldValue) writeLong(address, newValue);
    }

    @Override
    default void replaceDouble(long address, double oldValue, double newValue) {
        if (readDouble(address) == oldValue) writeDouble(address, newValue);
    }

    @Override
    default void replaceBoolean(long address, boolean oldValue, boolean newValue) {
        if (readBoolean(address) == oldValue) writeBoolean(address, newValue);
    }

    @Override
    default void writeInt(long address, int value) {
        writeMemoryInt(address, value);
    }

    @Override
    default void writeLong(long address, long value) {
        writeMemoryLong(address, value);
    }

    @Override
    default void writeDouble(long address, double value) {
        writeMemoryDouble(address, value);
    }

    @Override
    default void writeBoolean(long address, boolean value) {
        writeInt(address, value ? 1 : 0);
    }

    @Override
    default long[] searchInt(int value, int maxSize) {
        return queryMemoryInt(value, maxSize);
    }

    @Override
    default long[] searchLong(long value, int maxSize) {
        return queryMemoryLong(value, maxSize);
    }

    @Override
    default long[] searchPattern(byte[] pattern, int maxSize) {
        return queryMemory(pattern, maxSize);
    }

    //WindowAPI
    @Override
    default void keyClick(int keyCode) {
        keyboardClick((char) keyCode);
    }
}
