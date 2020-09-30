package com.github.manolo8.darkbot.core.utils;

public class ByteUtils {

    /**
     * The AtomConstants namespace defines constants for
     * manipulating atoms.
     *
     * The atom is a primitive value in ActionScript.  Since
     * ActionScript is a dynamically typed language, an atom can
     * belong to one of several types: null, undefined, number,
     * integer, string, boolean, object reference.
     *
     * Atoms are encoded with care to take up the minimum
     * possible space.  An atom is represented by a 32-bit
     * integer, with the bottom 3 bits indicating the type.
     *
     *      32 bit atom
     *
     *  31             16 15     8 7   3 210
     *  dddddddd dddddddd dddddddd ddddd TTT
     *
     *  TTT
     *  000  - untagged
     *  001  object
     *  010  string
     *  011  namespace
     *  100  undefined
     *  101  boolean
     *  110  integer
     *  111  double
     *
     *  - using last 3 bits means allocations must be 8-byte aligned.
     *  - related types are 1 bit apart, e.g. int/double
     *
     *  kIntptrType atoms are used to represent integer values from -2^28..2^28-1,
     *  regardless of whether the context implies int, uint, or Number.
     *  If a number doesn't fit into that range it is stored as a kDoubleType
     *
     */
    /**
     * A mask that will remove atom constant bits
     */
    public static final long ATOM_MASK = ~0b111L;
    @Deprecated // Use ATOM_MASK instead.
    public static final long FIX = ~0b111L;

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