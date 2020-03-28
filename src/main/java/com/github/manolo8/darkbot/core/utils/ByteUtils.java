package com.github.manolo8.darkbot.core.utils;

public class ByteUtils {
    public static final long FIX = 0xfffffffffff8L;

    public static int getInt(byte[] data, int offset) {
        return data.length < offset + 4 ? 0 : ((data[offset + 3]) << 24) |
                ((data[offset + 2] & 0xff) << 16) |
                ((data[offset + 1] & 0xff) << 8) |
                ((data[offset] & 0xff));
    }

    public static long getLong(byte[] data, int offset) {
        return data.length < offset + 8 ? 0 : (((long) data[offset + 7]) << 56) |
                (((long) data[offset + 6] & 0xff) << 48) |
                (((long) data[offset + 5] & 0xff) << 40) |
                (((long) data[offset + 4] & 0xff) << 32) |
                (((long) data[offset + 3] & 0xff) << 24) |
                (((long) data[offset + 2] & 0xff) << 16) |
                (((long) data[offset + 1] & 0xff) << 8) |
                (((long) data[offset] & 0xff));
    }

    public static byte[] getBytes(long... values) {
        byte[] b = new byte[values.length * 8];

        int i = 0;
        for (long value : values) {
            b[i++] = (byte) value;
            b[i++] = (byte) (value >> 8);
            b[i++] = (byte) (value >> 16);
            b[i++] = (byte) (value >> 24);
            b[i++] = (byte) (value >> 32);
            b[i++] = (byte) (value >> 40);
            b[i++] = (byte) (value >> 48);
            b[i++] = (byte) (value >> 56);
        }

        return b;
    }
}
