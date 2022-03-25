package com.github.manolo8.darkbot.core.api.util;

public interface DataReader extends AutoCloseable {

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

    byte[] toArray();
    byte[] setArray(byte[] dst, int pos, int length);

    @Override
    void close();
}
