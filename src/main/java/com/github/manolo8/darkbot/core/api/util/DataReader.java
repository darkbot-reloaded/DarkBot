package com.github.manolo8.darkbot.core.api.util;

public interface DataReader extends DataBuffer {

    enum Result {
        OK, ERROR, BUSY
    }

    /**
     * Read data from in-game to the data buffer.
     * Once read is called, this buffer becomes {@link Result#BUSY},
     * and any further attempts to this function will return {@link Result#BUSY},
     * until {@link #close()} is called.
     *
     * @param address The address to read from.
     * @param length How many bytes to read.
     * @return The result of the operation.
     */
    Result read(long address, int length);

    void reset(int limit);

}
