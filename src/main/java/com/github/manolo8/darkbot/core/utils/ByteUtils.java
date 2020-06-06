package com.github.manolo8.darkbot.core.utils;

public class ByteUtils {
    public static final long FIX = 0xfffffffffff8L;

    public static int getInt(byte[] data, int offset) {
        return data.length < offset + 4 ? 0 :
                ((data[offset + 3]) << 24) |
                ((data[offset + 2] & 0xff) << 16) |
                ((data[offset + 1] & 0xff) << 8) |
                ((data[offset] & 0xff));
    }

    public static long getLong(byte[] data, int offset) {
        return data.length < offset + 8 ? 0 :
                (((long) data[offset + 7]) << 56) |
                (((long) data[offset + 6] & 0xff) << 48) |
                (((long) data[offset + 5] & 0xff) << 40) |
                (((long) data[offset + 4] & 0xff) << 32) |
                (((long) data[offset + 3] & 0xff) << 24) |
                (((long) data[offset + 2] & 0xff) << 16) |
                (((long) data[offset + 1] & 0xff) << 8) |
                (((long) data[offset] & 0xff));
    }

    public static double getDouble(byte[] data, int offset) {
        return Double.longBitsToDouble(getLong(data, offset));
    }

    public static byte[] getBytes(int... values) {
        byte[] b = new byte[values.length * 4];

        int i = 0;
        for (int v : values) {
            b[i++] = (byte) ((v) & 0xff);
            b[i++] = (byte) ((v >>> 8)  & 0xff);
            b[i++] = (byte) ((v >>> 16) & 0xff);
            b[i++] = (byte) ((v >>> 24) & 0xff);
        }

        return b;
    }

    public static byte[] getBytes(long... values) {
        byte[] b = new byte[values.length * 8];

        int i = 0;
        for (long v : values) {
            b[i++] = (byte) v;
            b[i++] = (byte) (v >> 8);
            b[i++] = (byte) (v >> 16);
            b[i++] = (byte) (v >> 24);
            b[i++] = (byte) (v >> 32);
            b[i++] = (byte) (v >> 40);
            b[i++] = (byte) (v >> 48);
            b[i++] = (byte) (v >> 56);
        }

        return b;
    }
}