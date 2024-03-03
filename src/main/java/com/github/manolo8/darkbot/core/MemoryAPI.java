package com.github.manolo8.darkbot.core;

import eu.darkbot.api.API;

/**
 * Provides access to read/write native memory
 * <p>
 * Those calls should be generally safe,
 * every access violation error is handled by native code
*/
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessivePublicCount", "unused"})
public interface MemoryAPI extends API.Singleton {
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

    default int readInt(long address, int... offsets) {
        int i = 0;
        for (; i < offsets.length - 1; i++) {
            address = readLong(address + offsets[i]);
        }

        return readInt(address + offsets[i]);
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

    default long readLong(long address, int... offsets) {
        for (int offset : offsets) {
            address = readLong(address + offset);
        }

        return address;
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

    default double readDouble(long address, int... offsets) {
        int i = 0;
        for (; i < offsets.length - 1; i++) {
            address = readLong(address + offsets[i]);
        }

        return readDouble(address + offsets[i]);
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

    default boolean readBoolean(long address, int... offsets) {
        int i = 0;
        for (; i < offsets.length - 1; i++) {
            address = readLong(address + offsets[i]);
        }

        return readBoolean(address + offsets[i]);
    }

    /**
     * Reads String from memory.
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
     * Reads {@link String} from memory.
     * Retruns fallback if memory read failed, empty string is valid!
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
     * Reads byte array from memory with given length.
     *
     * @param address to read
     * @param length  of bytes to read
     * @return byte array from memory
     */
    byte[] readBytes(long address, int length);

    default byte[] readBytes(long address, int length, int... offsets) {
        int i = 0;
        for (; i < offsets.length - 1; i++) {
            address = readLong(address + offsets[i]);
        }

        return readBytes(address + offsets[i], length);
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
    void writeBoolean(long address, boolean value);

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
}
