package com.github.manolo8.darkbot.core.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.api.GameAPI;
import eu.darkbot.util.Timer;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.function.LongPredicate;

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
    @Deprecated // Use ATOM_MASK instead.
    public static final long FIX = ~0b111L;
    public static final long ATOM_KIND = 0b111L;
    public static final long ATOM_MASK = ~ATOM_KIND;

    public static final int OBJECT_TYPE = 0b001;
    public static final int STRING_TYPE = 0b010;

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

    public static boolean isScriptableObjectValid(long scriptableObject) {
        // contains references count & GC flags
        int composite = Main.API.readInt(scriptableObject + 8);
        return composite > 0;
    }

    public static long getClassClosure(long scriptObject) {
        long adr = Main.API.readLong(scriptObject, 0x18, 0x20) & ATOM_MASK;
        return Main.API.readLong(adr + 0x10) & ATOM_MASK;
    }

    public static String readObjectName(long object) {
        return Main.API.readString(object, 0x10, 0x28, 0x90);
    }

    public static class ExtraMemoryReader implements GameAPI.ExtraMemoryReader {
        private static final int TYPE_DYNAMIC = 0, TYPE_STATIC = 1, TYPE_DEPENDENT = 2;
        private static final int WIDTH_AUTO = -1, WIDTH_8 = 0, WIDTH_16 = 1;

        private final GameAPI.Memory reader;
        private final BotInstaller botInstaller;
        private final WeakValueHashMap<StrLocation, String> stringCache = new WeakValueHashMap<>();

        public ExtraMemoryReader(GameAPI.Memory reader, BotInstaller botInstaller) {
            this.reader = reader;
            this.botInstaller = botInstaller;
        }

        private class StrLocation {
            private long address, base, sizeAndFlags;
            private int size;
            private boolean width8, dependent;

            private StrLocation() {}

            private StrLocation(StrLocation copy) {
                this.address = copy.address;
                this.base = copy.base;
                this.size = copy.size;
                this.width8 = copy.width8;
                this.sizeAndFlags = copy.sizeAndFlags;
                this.dependent = copy.dependent;
            }

            private void setAddress(long address) {
                this.address = address;

                this.sizeAndFlags = reader.readLong(address + 32);
                int size = (int) sizeAndFlags; // lower 32bits
                if (size == 0) {
                    this.size = 0;
                    this.base = 0;
                    this.width8 = true;
                    return;
                }

                int flags  = (int) (sizeAndFlags >> 32); // high 32bits
                this.dependent = ((flags & 0b110) >> 1) == TYPE_DEPENDENT;
                int width = (flags & 0b001);

                this.size  = (size << width);
                this.width8 = width == WIDTH_8;
                if (dependent)
                    this.base = reader.readLong(address, 24, 16) + reader.readInt(address + 16);
                else
                    this.base = reader.readLong(address + 16);
            }

            private @Nullable String read() {
                if (size == 0) return "";
                // assume that string sizes over 1024 or below 0 are invalid
                if (size > 1024 || size < 0) return null;

                return new String(reader.readBytes(base, size),
                        width8 ? StandardCharsets.ISO_8859_1 : StandardCharsets.UTF_16LE);
            }

            private boolean hasChanged() {
                long sizeAndFlags = reader.readLong(address + 32);
                if (sizeAndFlags != this.sizeAndFlags) return true;

                long base;
                if (dependent) base = reader.readLong(address, 24, 16) + reader.readInt(address + 16);
                else base = reader.readLong(address + 16);

                return base != this.base;
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

        // Reusable instance of StrLocation to avoid extra allocations
        private final StrLocation CACHED = new StrLocation();

        public String readString(long address) {
            if (address <= 0xFFFF || !isScriptableObjectValid(address)) return null;
            if (Main.API.readLong(address) != BotInstaller.STRING_OBJECT_VTABLE)
                return null;
            CACHED.setAddress(address);

            // Attempt read in cache
            String result = stringCache.get(CACHED);
            if (result != null) return result;

            result = CACHED.read();
            if (result != null && !result.isEmpty()) {
                if (CACHED.hasChanged()) {
                    System.out.println("String header has changed!"); // return null maybe?
                    return null;
                } else stringCache.put(new StrLocation(CACHED), result);
            }

            return result;
        }

        public void resetCache() {
            stringCache.clear();
        }

        @Override
        public int getVersion() {
            return 1;
        }

        private static final int MAX_TABLE_SIZE = 2 << 20; // 2MB

        private final Timer timer = Timer.get(750);
        private byte[] tableData = null;

        /**
         * May cache the indexes.
         * Index for closures is always the same
         * @author Alph4rd
         */
        @Override
        public long searchClassClosure(LongPredicate pattern) {
            long mainAddress = botInstaller.mainApplicationAddress.get();
            if (mainAddress == 0) return 0;

            long table = Main.API.readMemoryLong(mainAddress, 0x10, 0x10, 0x18, 0x10, 0x28);
            int capacity = Main.API.readMemoryInt(table + 8) * 8; // capacity generally is always the same.

            if (capacity > MAX_TABLE_SIZE) return 0;

            if (tableData == null || tableData.length < capacity) {
                tableData = new byte[capacity];
                Main.API.readMemory(table + 0x10, tableData, capacity);
                timer.activate();

            } else if (timer.tryActivate())
                Main.API.readMemory(table + 0x10, tableData, capacity);

            for (int i = 0; i < capacity; i += 8) {
                long entry = ByteUtils.getLong(tableData, i);
                if (entry == 0) continue;

                long closure = Main.API.readMemoryLong(entry + 0x20);
                if (closure == 0 || closure == 0x200000001L) continue;

                if (pattern.test(closure)) return closure;
            }
            return 0;
        }

        @Override
        public void tick() {
            if (tableData == null || timer.isActive()) return;

            // keep the table data for 15 seconds if it's not used
            if (timer.getRemainingFuse() < -15_000) tableData = null;
        }
    }
}