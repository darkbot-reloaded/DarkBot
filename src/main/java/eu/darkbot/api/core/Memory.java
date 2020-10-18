package eu.darkbot.api.core;

/**
 * This interface provides API to read/write native memory
 * <p>
 * Those calls should be generally safe,
 * every access violation error is handled by native code
 */
public interface Memory {

    /**
     * Reads signed integer value from memory.
     *
     * @param address to read
     * @return signed integer value
     */
    int readInt(long address);
    int readInt(long address, int... offsets);

    /**
     * Reads signed long value from memory.
     *
     * @param address to read
     * @return signed long value
     */
    long readLong(long address);
    long readLong(long address, int... offsets);

    /**
     * Reads double value from memory.
     *
     * @param address to read
     * @return double value
     */
    double readDouble(long address);
    double readDouble(long address, int... offsets);

    /**
     * Reads boolean value from memory.
     *
     * @param address to read
     * @return boolean value
     */
    boolean readBoolean(long address);
    boolean readBoolean(long address, int... offsets);

    /**
     * Reads String from memory.
     *
     * @param address to read
     */
    String readString(long address);
    String readString(long address, int offsets);

    /**
     * Reads byte array from memory with given length.
     *
     * @param address to read
     * @param length  of bytes to read
     * @return byte array from memory
     */
    byte[] readBytes(long address, int length);
    byte[] readBytes(long address, int length, int... offsets);

    /**
     * Replaces integer value in memory with newValue only if
     * oldValue matches current value in memory.
     *
     * @param address  to be replaced
     * @param oldValue which will be matched
     * @param newValue which will be written
     */
    void replaceInt(long address, int oldValue, int newValue);
    void replaceInt(long address, int oldValue, int newValue, int... offsets);

    /**
     * Replaces long value in memory with newValue only if
     * oldValue matches current value in memory.
     *
     * @param address  to be replaced
     * @param oldValue which will be matched
     * @param newValue which will be written
     */
    void replaceLong(long address, long oldValue, long newValue);
    void replaceLong(long address, long oldValue, long newValue, int... offsets);

    /**
     * Replaces double value in memory with newValue only if
     * oldValue matches current value in memory.
     *
     * @param address  to be replaced
     * @param oldValue which will be matched
     * @param newValue which will be written
     */
    void replaceDouble(long address, double oldValue, double newValue);
    void replaceDouble(long address, double oldValue, double newValue, int... offsets);

    /**
     * Replaces boolean value in memory with newValue only if
     * oldValue matches current value in memory.
     *
     * @param address  to be replaced
     * @param oldValue which will be matched
     * @param newValue which will be written
     */
    void replaceBoolean(long address, boolean oldValue, boolean newValue);
    void replaceBoolean(long address, boolean oldValue, boolean newValue, int... offsets);

    /**
     * Overrides memory at address with given integer value
     *
     * @param address to be written at
     * @param value   which will be written
     */
    void writeInt(long address, int value);
    void writeInt(long address, int value, int... offsets);

    /**
     * Overrides memory at address with given long value
     *
     * @param address to be written at
     * @param value   which will be written
     */
    void writeLong(long address, long value);
    void writeLong(long address, long value, int... offsets);

    /**
     * Overrides memory at address with given double value
     *
     * @param address to be written at
     * @param value   which will be written
     */
    void writeDouble(long address, double value);
    void writeDouble(long address, double value, int... offsets);

    /**
     * Overrides memory at address with given boolean value
     *
     * @param address to be written at
     * @param value   which will be written
     */
    void writeBoolean(long address, boolean value);
    void writeBoolean(long address, boolean value, int... offsets);

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
    long[] queryInt(int value, int maxSize, int... offsets);

    /**
     * Search current process memory for given value
     * until it reaches maxSize array length or no more memory regions to be searched.
     *
     * @param value   to look for
     * @param maxSize max length of returned array
     * @return array of direct pointers to searched value
     */
    long[] queryLong(long value, int maxSize);
    long[] queryLong(long value, int maxSize, int... offsets);

    /**
     * Search current process memory for given pattern
     * until it reaches maxSize array length or no more memory regions to be searched.
     *
     * @param pattern to look for
     * @param maxSize max length of returned array
     * @return array of direct pointers to searched pattern
     */
    long[] queryBytes(byte[] pattern, int maxSize);
    long[] queryBytes(byte[] pattern, int maxSize, int... offsets);
}
