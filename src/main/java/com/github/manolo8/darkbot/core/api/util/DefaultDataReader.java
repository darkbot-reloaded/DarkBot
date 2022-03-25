package com.github.manolo8.darkbot.core.api.util;

import com.github.manolo8.darkbot.core.api.GameAPI;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DefaultDataReader extends AbstractDataReader {

    private final GameAPI.Memory memory;
    private final byte[] buffer = new byte[DataReader.MAX_CHUNK_SIZE];
    private final ByteBuffer byteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.nativeOrder());

    public DefaultDataReader(GameAPI.Memory memory, GameAPI.ExtraMemoryReader reader) {
        super(reader);
        this.memory = memory;
    }

    @Override
    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    @Override
    public Boolean read(long address, int length) {
        if (!inUse.compareAndSet(false, true)) return false;

        memory.readBytes(address, buffer, length);
        reset(length);

        return true;
    }
}
