package com.github.manolo8.darkbot.core;

import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.core.api.util.DataReader;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.managers.MemoryAPI;
import eu.darkbot.api.managers.OreAPI;
import eu.darkbot.api.managers.WindowAPI;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface IDarkBotAPI extends WindowAPI, MemoryAPI {

    void tick();

    void createWindow();
    void setSize(int width, int height);

    boolean isValid();
    boolean isInitiallyShown();
    long getMemoryUsage();
    String getVersion();

    void mouseMove(int x, int y);
    void mouseDown(int x, int y);
    void mouseUp(int x, int y);
    void mouseClick(int x, int y);

    @Deprecated
    default void keyboardClick(char btn) {
        rawKeyboardClick(Character.toUpperCase(btn));
    }

    void rawKeyboardClick(char btn);

    default void keyboardClick(Character ch) {
        if (ch != null) rawKeyboardClick(ch);
    }

    @Override
    default void keyClick(int keyCode) {
        rawKeyboardClick((char) keyCode);
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

    /**
     * Reads data at given address, use always with try-with-resources
     * Length limit is {@link DataReader#MAX_CHUNK_SIZE}
     *
     * @param address to read
     * @param length  the length of the data to read
     * @return {@link DataReader}
     */
    DataReader readData(long address, int length);

    /**
     * Length limit is {@link DataReader#MAX_CHUNK_SIZE}
     *
     * @param address to read
     * @param length  the length of the data to read
     * @param reader  consumer which will be used if read was success
     * @return false if read failed, true otherwise
     */
    boolean readData(long address, int length, Consumer<DataReader> reader);

    void writeMemoryInt(long address, int value);
    void writeMemoryLong(long address, long value);
    void writeMemoryDouble(long address, double value);

    long[] queryMemoryInt(int value, int maxQuantity);
    long[] queryMemoryLong(long value, int maxQuantity);
    long[] queryMemory(byte[] query, int maxQuantity);

    long searchClassClosure(Predicate<Long> pattern);

    void setVisible(boolean visible);
    default void setMinimized(boolean minimized) {
        setVisible(!minimized);
    }

    default void setVisible(boolean visible, boolean fullyHideEnabled) {
        if (fullyHideEnabled) setMinimized(!visible);
        else setVisible(visible);
    }

    void handleRefresh();
    void resetCache();

    boolean hasCapability(GameAPI.Capability capability);

    // Direct game access
    void setMaxFps(int maxCps);
    void lockEntity(int id);
    void moveShip(Locatable destination);
    void collectBox(Locatable destination, long collectableAddress);
    void refine(long refineUtilAddress, OreAPI.Ore ore, int amount);
    long callMethod(int index, long... arguments);

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
    void replaceInt(long address, int oldValue, int newValue);

    @Override
    void replaceLong(long address, long oldValue, long newValue);

    @Override
    void replaceDouble(long address, double oldValue, double newValue);

    @Override
    void replaceBoolean(long address, boolean oldValue, boolean newValue);

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
    default long[] searchPattern(int maxSize, byte... pattern) {
        return queryMemory(pattern, maxSize);
    }

}
