package com.github.manolo8.darkbot.behaviours.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;

import static com.github.manolo8.darkbot.Main.API;

/**
 * Consecutive 8-byte words of client memory, read in 64 KB chunks: one native call per chunk instead of one per
 * word. The buffer grows to the largest read and is kept until {@link #release()}.
 */
final class WordBuffer {

    private static final int CHUNK_BYTES = 64 * 1024;

    private final byte[] chunk = new byte[CHUNK_BYTES];
    private final LongBuffer chunkWords = ByteBuffer.wrap(chunk).order(ByteOrder.LITTLE_ENDIAN).asLongBuffer();
    private long[] words = new long[0];

    /** Reads {@code count} words starting at the address. The caller bounds the count. */
    void read(long address, int count) {
        if (words.length < count) words = new long[count];
        for (int first = 0; first < count; first += CHUNK_BYTES / Long.BYTES) {
            int length = Math.min(CHUNK_BYTES / Long.BYTES, count - first);
            API.readBytes(address + (long) first * Long.BYTES, chunk, length * Long.BYTES);
            chunkWords.clear();
            chunkWords.get(words, first, length);
        }
    }

    long get(int index) {
        return words[index];
    }

    void release() {
        words = new long[0];
    }
}
