package eu.darkbot.api.managers;

import eu.darkbot.api.API;

/**
 * This interface provides API to read/write native memory
 * <p>
 * Those calls should be generally safe,
 * every access violation error is handled by native code
 */
public interface MemoryAPI extends API {

    /**
     * Reads signed integer value from memory.
     *
     * @param address to read
     * @return signed integer value
     */
    int readInt(long address);

    default int readInt(long address, int... offsets) {
        int i = 0;
        for (; i < offsets.length - 1; i++)
            address = readLong(address + offsets[i]);

        return readInt(address + offsets[i]);
    }

    /**
     * Reads signed long value from memory.
     *
     * @param address to read
     * @return signed long value
     */
    long readLong(long address);

    default long readLong(long address, int... offsets) {
        for (int offset : offsets)
            address = readLong(address + offset);

        return address;
    }

    /**
     * Reads double value from memory.
     *
     * @param address to read
     * @return double value
     */
    double readDouble(long address);

    default double readDouble(long address, int... offsets) {
        int i = 0;
        for (; i < offsets.length - 1; i++)
            address = readLong(address + offsets[i]);

        return readDouble(address + offsets[i]);
    }

    /**
     * Reads boolean value from memory.
     *
     * @param address to read
     * @return boolean value
     */
    boolean readBoolean(long address);

    default boolean readBoolean(long address, int... offsets) {
        int i = 0;
        for (; i < offsets.length - 1; i++)
            address = readLong(address + offsets[i]);

        return readBoolean(address + offsets[i]);
    }

    /**
     * Reads String from memory.
     *
     * @param address to read
     */
    String readString(long address);

    default String readString(long address, int... offsets) {
        return readString(readLong(address, offsets));
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
        for (; i < offsets.length - 1; i++)
            address = readLong(address + offsets[i]);

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
        for (; i < offsets.length - 1; i++)
            address = readLong(address + offsets[i]);

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
        for (; i < offsets.length - 1; i++)
            address = readLong(address + offsets[i]);

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
        for (; i < offsets.length - 1; i++)
            address = readLong(address + offsets[i]);

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
        for (; i < offsets.length - 1; i++)
            address = readLong(address + offsets[i]);

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
        for (; i < offsets.length - 1; i++)
            address = readLong(address + offsets[i]);

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
        for (; i < offsets.length - 1; i++)
            address = readLong(address + offsets[i]);

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
        for (; i < offsets.length - 1; i++)
            address = readLong(address + offsets[i]);

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
        for (; i < offsets.length - 1; i++)
            address = readLong(address + offsets[i]);

        writeBoolean(address + offsets[i], value);
    }

    /**
     * Overrides memory region at given address with bytes array.
     * Region from (address) to (address + bytes.length) will be overridden.
     *
     * @param address where writing starts
     * @param bytes   to be written
     */
    void writeBytes(long address, byte... bytes);

    /**
     * Search current process memory for given value
     * until it reaches maxSize array length or no more memory regions to be searched.
     *
     * @param value   to look for
     * @param maxSize max length of returned array
     * @return array of direct pointers to searched value
     */
    long[] queryInt(int value, int maxSize);

    /**
     * Search current process memory for given value
     * until it reaches maxSize array length or no more memory regions to be searched.
     *
     * @param value   to look for
     * @param maxSize max length of returned array
     * @return array of direct pointers to searched value
     */
    long[] queryLong(long value, int maxSize);

    /**
     * Search current process memory for given pattern
     * until it reaches maxSize array length or no more memory regions to be searched.
     *
     * @param pattern to look for
     * @param maxSize max length of returned array
     * @return array of direct pointers to searched pattern
     */
    long[] queryBytes(byte[] pattern, int maxSize);
}
