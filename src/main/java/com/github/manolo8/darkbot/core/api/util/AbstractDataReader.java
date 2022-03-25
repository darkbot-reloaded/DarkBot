package com.github.manolo8.darkbot.core.api.util;

import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractDataReader implements DataReader {

    private final GameAPI.ExtraMemoryReader reader;

    protected final AtomicBoolean inUse = new AtomicBoolean();

    public AbstractDataReader(GameAPI.ExtraMemoryReader reader) {
        this.reader = reader;
    }

    public abstract ByteBuffer getByteBuffer();

    // if returns null, something bad happen while reading the data
    public abstract Boolean read(long address, int length);

    public void reset(int limit) {
        getByteBuffer().clear();
        getByteBuffer().limit(limit);
    }

    @Override
    public int getPosition() {
        return getByteBuffer().position();
    }

    @Override
    public void setPosition(int pos) {
        getByteBuffer().position(pos);
    }

    @Override
    public int getLimit() {
        return getByteBuffer().limit();
    }

    @Override
    public int getAvailable() {
        return getByteBuffer().remaining();
    }

    @Override
    public byte getByte() {
        return getByteBuffer().get();
    }

    @Override
    public byte getByte(int idx) {
        return getByteBuffer().get(idx);
    }

    @Override
    public boolean getBoolean() {
        return getInt() == 1;
    }

    @Override
    public boolean getBoolean(int idx) {
        return getByte(idx) == 1;
    }

    @Override
    public short getShort() {
        return getByteBuffer().getShort();
    }

    @Override
    public short getShort(int idx) {
        return getByteBuffer().getShort(idx);
    }

    @Override
    public int getInt() {
        return getByteBuffer().getInt();
    }

    @Override
    public int getInt(int idx) {
        return getByteBuffer().getInt(idx);
    }

    @Override
    public long getLong() {
        return getByteBuffer().getLong();
    }

    @Override
    public long getLong(int idx) {
        return getByteBuffer().getLong(idx);
    }

    @Override
    public long getPointer() {
        return getByteBuffer().getLong() & ByteUtils.ATOM_MASK;
    }

    @Override
    public long getPointer(int idx) {
        return getByteBuffer().getLong(idx) & ByteUtils.ATOM_MASK;
    }

    @Override
    public double getDouble() {
        return getByteBuffer().getDouble();
    }

    @Override
    public double getDouble(int idx) {
        return getByteBuffer().getDouble(idx);
    }

    @Override
    public String getString() {
        return reader.readString(getLong());
    }

    @Override
    public String getString(int idx) {
        return reader.readString(getLong(idx));
    }

    @Override
    public byte[] toArray() {
        return getByteBuffer().array();
    }

    @Override
    public byte[] setArray(byte[] dst, int pos, int length) {
        getByteBuffer().get(dst, pos, length);
        return dst;
    }

    @Override
    public void close() {
        inUse.set(false);
    }
}
