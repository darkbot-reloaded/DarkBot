package com.github.manolo8.darkbot.core.api.util;

public interface DataBuffer extends AutoCloseable {

    int MAX_CHUNK_SIZE = 2048;

    int getPosition();
    void setPosition(int pos);

    int getLimit();
    int getAvailable();

    byte getByte();
    byte getByte(int idx);

    boolean getBoolean();
    boolean getBoolean(int idx);

    short getShort();
    short getShort(int idx);

    int getInt();
    int getInt(int idx);

    long getLong();
    long getLong(int idx);

    /**
     * @return readLong() & ATOM_MASK
     */
    long getPointer();
    long getPointer(int idx);

    double getDouble();
    double getDouble(int idx);

    String getString();
    String getString(int idx);

    /**
     * This method allocates a new array and copies the bytes from this buffer into it.
     * Using the method is not advised as it generates new allocations on each call.
     * Prefer using {@link #getArray(byte[], int, int)} instead.
     *
     * @return a new array containing the bytes from this buffer.
     */
    byte[] toArray();

    /**
     * This method transfers bytes from this buffer into the given destination array.
     *
     * @param dst The array into which bytes are to be written.
     * @param offset The offset within the array of the first byte to be written;
     *               must be non-negative and no larger than {@code dst.length}
     * @param length The maximum number of bytes to be written to the given array; must be non-negative
     *               and no larger than {@code dst.length - offset}
     * @return The dst param, useful for chaining
     */
    byte[] getArray(byte[] dst, int offset, int length);

    @Override
    void close();
}
