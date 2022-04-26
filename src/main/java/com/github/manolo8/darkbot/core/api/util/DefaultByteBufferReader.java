package com.github.manolo8.darkbot.core.api.util;

import com.github.manolo8.darkbot.core.api.GameAPI;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DefaultByteBufferReader extends ByteBufferReader implements DataReader {

    private final GameAPI.Memory memory;
    private final byte[] buffer;

    private DefaultByteBufferReader(byte[] buffer, GameAPI.Memory memory, GameAPI.ExtraMemoryReader reader) {
        super(ByteBuffer.wrap(buffer).order(ByteOrder.nativeOrder()), reader);
        this.buffer = buffer;
        this.memory = memory;
    }

    public static DefaultByteBufferReader of(GameAPI.Memory memory, GameAPI.ExtraMemoryReader reader) {
        return new DefaultByteBufferReader(new byte[DataBuffer.MAX_CHUNK_SIZE], memory, reader);
    }

    @Override
    public DataReader.Result read(long address, int length) {
        if (!inUse.compareAndSet(false, true)) return DataReader.Result.BUSY;

        memory.readBytes(address, buffer, length);
        reset(length);

        return Result.OK;
    }
}
