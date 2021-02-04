package com.github.manolo8.darkbot.core.utils;

import com.github.manolo8.darkbot.core.IDarkBotAPI;

import java.nio.charset.StandardCharsets;

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
     * A mask that will remove atom constant bits:
     */
    public static final long ATOM_MASK = ~0b111L;
    @Deprecated // Use ATOM_MASK instead.
    public static final long FIX = ~0b111L;

    /**
     * Constant value which means that reference to the object,
     * is invalid/doesn't exists and shouldn't be updated.
     */
    public static final long NULL = 0;

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

    public static class StringReader {
        private static final int TYPE_DYNAMIC = 0, TYPE_STATIC = 1, TYPE_DEPENDENT = 2;
        private static final int WIDTH_AUTO = -1, WIDTH_8 = 0, WIDTH_16 = 1;

        private final IDarkBotAPI API;
        private final WeakValueHashMap<StrLocation, String> stringCache = new WeakValueHashMap<>();

        public StringReader(IDarkBotAPI API) {
            this.API = API;
        }

        private class StrLocation {
            private final long address, base;
            private final int size;
            private final boolean width8;

            private StrLocation(long address) {
                this.address = address;

                int size = API.readMemoryInt(address + 32);
                if (size == 0) {
                    this.size = 0;
                    this.base = 0;
                    this.width8 = true;
                    return;
                }

                int flags  = API.readMemoryInt(address + 36);
                int type   = (flags & 0b110) >> 1;
                int width = (flags & 0b001);

                this.size  = (size << width);
                this.width8 = width == WIDTH_8;
                if (type == TYPE_DEPENDENT)
                    this.base = API.readMemoryLong(address, 24, 16) + API.readMemoryInt(address + 16);
                else
                    this.base = API.readMemoryLong(address + 16);
            }

            private String read() {
                if (size == 0) return "";
                // assume that string sizes over 1024 or below 0 are invalid
                if (size > 1024 || size < 0) return null;

                return new String(API.readMemory(base, size),
                        width8 ? StandardCharsets.ISO_8859_1 : StandardCharsets.UTF_16LE);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                StrLocation that = (StrLocation) o;

                if (address != that.address) return false;
                if (base != that.base) return false;
                if (size != that.size) return false;
                return width8 == that.width8;
            }

            @Override
            public int hashCode() {
                int result = (int) (address ^ (address >>> 32));
                result = 31 * result + (int) (base ^ (base >>> 32));
                result = 31 * result + size;
                result = 31 * result + (width8 ? 1 : 0);
                return result;
            }
        }

        public String readString(long address) {
            if (address == 0) return "";
            StrLocation loc = new StrLocation(address);

            // Attempt read in cache
            String result = stringCache.get(loc);
            if (result != null) return result;

            result = loc.read();
            if (result != null && !result.isEmpty())
                stringCache.put(loc, result);

            return result;
        }

    }


}