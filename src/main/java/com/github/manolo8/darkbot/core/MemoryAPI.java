package com.github.manolo8.darkbot.core;

import eu.darkbot.api.API;

import java.util.function.LongPredicate;

/**
 * Provides access to read/write native memory
 * <p>
 * Those calls should be generally safe,
 * every access violation error is handled by native code
 */
public interface MemoryAPI extends API.Singleton {
    int BOOL_FALSE = 0, BOOL_TRUE = 1, BINDABLE_INT_VALUE_OFFSET = 0x38;

    long NULL = 0;
    long ATOM_KIND = 0b111L;
    long ATOM_MASK = ~ATOM_KIND;

    /**
     * String used as fallback for {@link #readString(long)} when returned value is null.
     */
    String FALLBACK_STRING = "ERROR";

    /**
     * Reads signed integer value from memory.
     *
     * @param address to read
     * @return signed integer value
     */
    int readInt(long address);

    default int readInt(long address, int o1) {
        return readInt(address + o1);
    }

    default int readInt(long address, int o1, int o2) {
        return readInt(readAtom(address, o1) + o2);
    }

    default int readInt(long address, int o1, int o2, int o3) {
        return readInt(readAtom(address, o1, o2) + o3);
    }

    default int readInt(long address, int o1, int o2, int o3, int o4) {
        return readInt(readAtom(address, o1, o2, o3) + o4);
    }

    default int readBindableInt(long address) {
        return clampDoubleToInt(readDouble(address, BINDABLE_INT_VALUE_OFFSET));
    }

    // Reads 'BindableInt' holder value
    default int readBindableInt(long address, int o1) {
        return clampDoubleToInt(readDouble(address, o1, BINDABLE_INT_VALUE_OFFSET));
    }

    default int readBindableInt(long address, int o1, int o2) {
        return clampDoubleToInt(readDouble(address, o1, o2, BINDABLE_INT_VALUE_OFFSET));
    }

    default int readBindableInt(long address, int o1, int o2, int o3) {
        return clampDoubleToInt(readDouble(address, o1, o2, o3, BINDABLE_INT_VALUE_OFFSET));
    }

    private int clampDoubleToInt(double value) {
        if (value > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (value < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) value;
    }

    /**
     * Reads signed long value from memory.
     *
     * @param address to read
     * @return signed long value
     */
    long readLong(long address);

    default long readLong(long address, int o1) {
        return readLong(address + o1);
    }

    default long readLong(long address, int o1, int o2) {
        return readLong(readLong(address, o1) + o2);
    }

    default long readLong(long address, int o1, int o2, int o3) {
        return readLong(readLong(address, o1, o2) + o3);
    }

    default long readLong(long address, int o1, int o2, int o3, int o4) {
        return readLong(readLong(address, o1, o2, o3) + o4);
    }

    default long readLong(long address, int o1, int o2, int o3, int o4, int o5) {
        return readLong(readLong(address, o1, o2, o3, o4) + o5);
    }

    default long readAtom(long address) {
        return readLong(address) & ATOM_MASK;
    }

    default long readAtom(long address, int o1) {
        return readAtom(address + o1);
    }

    default long readAtom(long address, int o1, int o2) {
        return readAtom(readAtom(address, o1) + o2) & ATOM_MASK;
    }

    default long readAtom(long address, int o1, int o2, int o3) {
        return readAtom(readAtom(address, o1, o2) + o3) & ATOM_MASK;
    }

    default long readAtom(long address, int o1, int o2, int o3, int o4) {
        return readAtom(readAtom(address, o1, o2, o3) + o4) & ATOM_MASK;
    }

    default long readAtom(long address, int o1, int o2, int o3, int o4, int o5) {
        return readAtom(readAtom(address, o1, o2, o3, o4) + o5) & ATOM_MASK;
    }

    /**
     * Reads double value from memory.
     *
     * @param address to read
     * @return double value
     */
    double readDouble(long address);

    default double readDouble(long address, int o1) {
        return readDouble(address + o1);
    }

    default double readDouble(long address, int o1, int o2) {
        return readDouble(readAtom(address, o1) + o2);
    }

    default double readDouble(long address, int o1, int o2, int o3) {
        return readDouble(readAtom(address, o1, o2) + o3);
    }

    default double readDouble(long address, int o1, int o2, int o3, int o4) {
        return readDouble(readAtom(address, o1, o2, o3) + o4);
    }

    /**
     * Reads boolean value from memory.
     *
     * @param address to read
     * @return boolean value
     */
    boolean readBoolean(long address);

    default boolean readBoolean(long address, int o1) {
        return readBoolean(address + o1);
    }

    default boolean readBoolean(long address, int o1, int o2) {
        return readBoolean(readAtom(address, o1) + o2);
    }

    default boolean readBoolean(long address, int o1, int o2, int o3) {
        return readBoolean(readAtom(address, o1, o2) + o3);
    }

    default boolean readBoolean(long address, int o1, int o2, int o3, int o4) {
        return readBoolean(readAtom(address, o1, o2, o3) + o4);
    }

    /**
     * Reads {@link String} from memory.
     * Results may be cached for later use.
     * Returns fallback if memory read failed, empty string is valid!
     *
     * @param address  to read from
     * @param fallback to return in case of invalid memory read
     * @return string from memory if present, fallback otherwise
     */
    String readString(long address, String fallback);

    default String readString(long address, String fallback, int o1) {
        return readString(readAtom(address, o1), fallback);
    }

    default String readString(long address, String fallback, int o1, int o2) {
        return readString(readAtom(address, o1, o2), fallback);
    }

    default String readString(long address, String fallback, int o1, int o2, int o3) {
        return readString(readAtom(address, o1, o2, o3), fallback);
    }

    default String readString(long address, String fallback, int o1, int o2, int o3, int o4) {
        return readString(readAtom(address, o1, o2, o3, o4), fallback);
    }

    default String readString(long address, String fallback, int o1, int o2, int o3, int o4, int o5) {
        return readString(readAtom(address, o1, o2, o3, o4, o5), fallback);
    }

    /**
     * Reads String from memory. Results may be cached for later use.
     *
     * @param address to read
     * @return the string from memory if present, {@link #FALLBACK_STRING} otherwise
     */
    default String readString(long address) {
        return readString(address, FALLBACK_STRING);
    }

    default String readString(long address, int o1) {
        return readString(readAtom(address, o1));
    }

    default String readString(long address, int o1, int o2) {
        return readString(readAtom(address, o1, o2));
    }

    default String readString(long address, int o1, int o2, int o3) {
        return readString(readAtom(address, o1, o2, o3));
    }

    default String readString(long address, int o1, int o2, int o3, int o4) {
        return readString(readAtom(address, o1, o2, o3, o4));
    }

    default String readString(long address, int o1, int o2, int o3, int o4, int o5) {
        return readString(readAtom(address, o1, o2, o3, o4, o5));
    }

    /**
     * Reads {@link String} from memory directly, without using any cache etc.
     * Returns fallback if memory read failed, empty string is valid!
     *
     * @param address  to read from
     * @param fallback to return in case of invalid memory read
     * @return string from memory if present, fallback otherwise
     */
    String readStringDirect(long address, String fallback);

    default String readStringDirect(long address, String fallback, int o1) {
        return readStringDirect(readAtom(address, o1), fallback);
    }

    default String readStringDirect(long address, String fallback, int o1, int o2) {
        return readStringDirect(readAtom(address, o1, o2), fallback);
    }

    default String readStringDirect(long address, String fallback, int o1, int o2, int o3) {
        return readStringDirect(readAtom(address, o1, o2, o3), fallback);
    }

    default String readStringDirect(long address, String fallback, int o1, int o2, int o3, int o4) {
        return readStringDirect(readAtom(address, o1, o2, o3, o4), fallback);
    }

    default String readStringDirect(long address, String fallback, int o1, int o2, int o3, int o4, int o5) {
        return readStringDirect(readAtom(address, o1, o2, o3, o4, o5), fallback);
    }

    /**
     * Reads String from memory directly, without using any cache etc.
     *
     * @param address to read
     * @return the string from memory if present, {@link #FALLBACK_STRING} otherwise
     */
    default String readStringDirect(long address) {
        return readStringDirect(address, FALLBACK_STRING);
    }

    default String readStringDirect(long address, int o1) {
        return readStringDirect(readAtom(address, o1));
    }

    default String readStringDirect(long address, int o1, int o2) {
        return readStringDirect(readAtom(address, o1, o2));
    }

    default String readStringDirect(long address, int o1, int o2, int o3) {
        return readStringDirect(readAtom(address, o1, o2, o3));
    }

    default String readStringDirect(long address, int o1, int o2, int o3, int o4) {
        return readStringDirect(readAtom(address, o1, o2, o3, o4));
    }

    default String readStringDirect(long address, int o1, int o2, int o3, int o4, int o5) {
        return readStringDirect(readAtom(address, o1, o2, o3, o4, o5));
    }

    /**
     * Reads byte array from memory with given length.
     *
     * @param address to read
     * @param length  of bytes to read
     * @return byte array from memory
     */
    byte[] readBytes(long address, int length);

    /**
     * Reads bytes from memory and stores them in the specified buffer.
     *
     * @param address the address to read from.
     * @param buffer  the buffer to store the read bytes.
     * @param length  the number of bytes to read.
     */
    void readBytes(long address, byte[] buffer, int length);

    /**
     * Reads bytes from memory and stores them in the provided buffer.
     * This method is an overload that assumes the buffer length is the same as the number of bytes to read.
     *
     * @param address the address to read from.
     * @param buffer  the buffer to store the read bytes (with a length equal to the number of bytes to read).
     */
    default void readBytes(long address, byte[] buffer) {
        readBytes(address, buffer, buffer.length);
    }

    /**
     * Replaces integer value in memory with newValue only if
     * oldValue matches current value in memory.
     *
     * @param address  to be replaced
     * @param oldValue which will be matched
     * @param newValue which will be written
     */
    void replaceInt(long address, int oldValue, int newValue);

    default void replaceInt(long address, int oldValue, int newValue, int... offsets) {
        int i = 0;
        for (; i < offsets.length - 1; i++) {
            address = readLong(address + offsets[i]);
        }

        replaceInt(address + offsets[i], oldValue, newValue);
    }

    /**
     * Replaces long value in memory with newValue only if
     * oldValue matches current value in memory.
     *
     * @param address  to be replaced
     * @param oldValue which will be matched
     * @param newValue which will be written
     */
    void replaceLong(long address, long oldValue, long newValue);

    default void replaceLong(long address, long oldValue, long newValue, int... offsets) {
        int i = 0;
        for (; i < offsets.length - 1; i++) {
            address = readLong(address + offsets[i]);
        }

        replaceLong(address + offsets[i], oldValue, newValue);
    }

    /**
     * Replaces double value in memory with newValue only if
     * oldValue matches current value in memory.
     *
     * @param address  to be replaced
     * @param oldValue which will be matched
     * @param newValue which will be written
     */
    void replaceDouble(long address, double oldValue, double newValue);

    default void replaceDouble(long address, double oldValue, double newValue, int... offsets) {
        int i = 0;
        for (; i < offsets.length - 1; i++) {
            address = readLong(address + offsets[i]);
        }

        replaceDouble(address + offsets[i], oldValue, newValue);
    }

    /**
     * Replaces boolean value in memory with newValue only if
     * oldValue matches current value in memory.
     *
     * @param address  to be replaced
     * @param oldValue which will be matched
     * @param newValue which will be written
     */
    void replaceBoolean(long address, boolean oldValue, boolean newValue);

    default void replaceBoolean(long address, boolean oldValue, boolean newValue, int... offsets) {
        int i = 0;
        for (; i < offsets.length - 1; i++) {
            address = readLong(address + offsets[i]);
        }

        replaceBoolean(address + offsets[i], oldValue, newValue);
    }

    /**
     * Overrides memory at address with given integer value
     *
     * @param address to be written at
     * @param value   which will be written
     */
    void writeInt(long address, int value);

    default void writeInt(long address, int value, int... offsets) {
        int i = 0;
        for (; i < offsets.length - 1; i++) {
            address = readLong(address + offsets[i]);
        }

        writeInt(address + offsets[i], value);
    }

    /**
     * Overrides memory at address with given long value
     *
     * @param address to be written at
     * @param value   which will be written
     */
    void writeLong(long address, long value);

    default void writeLong(long address, long value, int... offsets) {
        int i = 0;
        for (; i < offsets.length - 1; i++) {
            address = readLong(address + offsets[i]);
        }

        writeLong(address + offsets[i], value);
    }

    /**
     * Overrides memory at address with given double value
     *
     * @param address to be written at
     * @param value   which will be written
     */
    void writeDouble(long address, double value);

    default void writeDouble(long address, double value, int... offsets) {
        int i = 0;
        for (; i < offsets.length - 1; i++) {
            address = readLong(address + offsets[i]);
        }

        writeDouble(address + offsets[i], value);
    }

    /**
     * Overrides memory at address with given boolean value
     *
     * @param address to be written at
     * @param value   which will be written
     */
    default void writeBoolean(long address, boolean value) {
        writeInt(address, value ? BOOL_TRUE : BOOL_FALSE);
    }

    default void writeBoolean(long address, boolean value, int... offsets) {
        int i = 0;
        for (; i < offsets.length - 1; i++) {
            address = readLong(address + offsets[i]);
        }

        writeBoolean(address + offsets[i], value);
    }

    /**
     * Search current process memory for given value
     * until it reaches maxSize array length or no more memory regions to be searched.
     *
     * @param value   to look for
     * @param maxSize max length of returned array
     * @return array of direct pointers to searched value
     */
    long[] searchInt(int value, int maxSize);

    /**
     * Search current process memory for given value
     * until it reaches maxSize array length or no more memory regions to be searched.
     *
     * @param value   to look for
     * @param maxSize max length of returned array
     * @return array of direct pointers to searched value
     */
    long[] searchLong(long value, int maxSize);

    /**
     * Search current process memory for given pattern
     * until it reaches maxSize array length or no more memory regions to be searched.
     *
     * @param maxSize max length of returned array
     * @param pattern to look for
     * @return array of direct pointers to searched pattern
     */
    long[] searchPattern(int maxSize, byte... pattern);

    /**
     * Searches the current process memory for the given pattern.
     * Returns a single direct pointer to the searched pattern.
     *
     * @param pattern The pattern to look for.
     * @return A direct pointer to the searched pattern.
     */
    long searchPattern(byte... pattern);

    /**
     * Searches the current process memory for a AVM class closure that matches the given predicate.
     *
     * @param pattern The predicate to match against class closures.
     * @return A direct pointer to the matched class closure.
     */
    long searchClassClosure(LongPredicate pattern);
}
