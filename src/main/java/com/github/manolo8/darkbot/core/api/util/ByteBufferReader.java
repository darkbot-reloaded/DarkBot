package com.github.manolo8.darkbot.core.api.util;

import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Generic DataReader implementation backed by a byte buffer.
 * May be extended to provide reading in different ways.
 */
public class ByteBufferReader implements DataBuffer {

    protected final ByteBuffer buffer;
    protected final GameAPI.ExtraMemoryReader reader;

    protected final AtomicBoolean inUse = new AtomicBoolean();

    public ByteBufferReader(ByteBuffer buffer, GameAPI.ExtraMemoryReader reader) {
        this.buffer = buffer;
        this.reader = reader;
    }

    public ByteBuffer getByteBuffer() {
        return buffer;
    }

    public void reset(int limit) {
        buffer.clear();
        buffer.limit(limit);
    }

    @Override
    public int getPosition() {
        return buffer.position();
    }

    @Override
    public void setPosition(int pos) {
        buffer.position(pos);
    }

    @Override
    public int getLimit() {
        return buffer.limit();
    }

    @Override
    public int getAvailable() {
        return buffer.remaining();
    }

    @Override
    public byte getByte() {
        return buffer.get();
    }

    @Override
    public byte getByte(int idx) {
        return buffer.get(idx);
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
        return buffer.getShort();
    }

    @Override
    public short getShort(int idx) {
        return buffer.getShort(idx);
    }

    @Override
    public int getInt() {
        return buffer.getInt();
    }

    @Override
    public int getInt(int idx) {
        return buffer.getInt(idx);
    }

    @Override
    public long getLong() {
        return buffer.getLong();
    }

    @Override
    public long getLong(int idx) {
        return buffer.getLong(idx);
    }

    @Override
    public long getPointer() {
        return buffer.getLong() & ByteUtils.ATOM_MASK;
    }

    @Override
    public long getPointer(int idx) {
        return buffer.getLong(idx) & ByteUtils.ATOM_MASK;
    }

    @Override
    public double getDouble() {
        return buffer.getDouble();
    }

    @Override
    public double getDouble(int idx) {
        return buffer.getDouble(idx);
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
        return buffer.array();
    }

    @Override
    public byte[] getArray(byte[] dst, int offset, int length) {
        buffer.get(dst, offset, length);
        return dst;
    }

    @Override
    public void close() {
        inUse.set(false);
    }
}
