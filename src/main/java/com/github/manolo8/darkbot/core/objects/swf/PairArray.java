package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.core.utils.Lazy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.github.manolo8.darkbot.Main.API;

/**
 * Reads arrays with pair of values
 * Instead of EntryArray & Dictionary
 */
public abstract class PairArray extends SwfPtrCollection {
    private boolean autoUpdatable, ignoreEmpty = true;

    public int size;

    private Pair[] pairs = new Pair[0];
    private Map<String, Lazy<Long>> lazy = new HashMap<>();

    protected PairArray() {}

    /**
     * Reads pairs of {@code Array} type
     */
    public static PairArray ofArray() {
        return new PairArray.Pairs();
    }

    /**
     * Reads pairs of {@code Dictionary} type
     */
    public static PairArray ofDictionary() {
        return new PairArray.Dictionary();
    }

    public PairArray setAutoUpdatable(boolean updatable) {
        this.autoUpdatable = updatable;
        return this;
    }

    public PairArray setIgnoreEmpty(boolean ignoreEmpty) {
        this.ignoreEmpty = ignoreEmpty;
        return this;
    }

    public void addLazy(String key, Consumer<Long> consumer) {
        addLazy(key, consumer, true);
    }

    public void addLazy(String key, Consumer<Long> consumer, boolean cacheValue) {
        this.lazy.computeIfAbsent(key, k -> (cacheValue ? new Lazy<>() : new Lazy.NoCache<>())).add(consumer);
    }

    public int getSize() {
        return Math.min(size, pairs.length);
    }

    public long getPtr(int idx) {
        return get(idx).value;
    }

    public long getPtr(String key) {
        Pair pair = get(key);
        return pair != null ? pair.value : 0L;
    }

    public Pair get(int idx) {
        return idx >= 0 && idx < size && idx < pairs.length ? pairs[idx] : null;
    }

    public Pair get(String key) {
        for (Pair pair : pairs)
            if (pair != null && pair.key != null && pair.key.equals(key))
                return pair;

        return null;
    }

    public boolean hasKey(String key) {
        for (int i = 0; i < size && i < pairs.length; i++)
            if (pairs[i] != null && pairs[i].key != null && pairs[i].key.equals(key)) return true;
        return false;
    }

    @Override
    public void update(long address) {
        super.update(address);
        if (autoUpdatable) update();
    }

    private static class Pairs extends PairArray {
        private static final byte[] BUFFER = new byte[2048];
        private static long BUFF_BASE;
        private static int BUFF_OFFSET = 0, BUFFER_SIZE = 0;

        private int offset;

        public boolean isInvalid(long addr) {
            return addr == 0;
        }

        public String getKey(long addr) {
            if (isInvalid(addr) || (addr & ByteUtils.ATOM_KIND) != ByteUtils.STRING_TYPE) return null;
            String key = API.readMemoryStringFallback(addr & ByteUtils.ATOM_MASK, null);
            return key == null || key.isEmpty() ? null : key;
        }

        private static long readLong(long base, int offset, int expectedOffset) {
            if (base != BUFF_BASE || BUFF_OFFSET + BUFFER_SIZE < offset + 8) {
                int size = expectedOffset - offset;
                if (size <= 16) return API.readMemoryLong(base + offset); // small amount, ignore buffering
                else API.readMemory(
                        (BUFF_BASE = base) + (BUFF_OFFSET = offset),
                        BUFFER,
                        BUFFER_SIZE = Math.min(size, BUFFER.length));
            }
            return ByteUtils.getLong(BUFFER, offset - BUFF_OFFSET);
        }

        public void update() {
            if (super.lazy.isEmpty() && super.ignoreEmpty) return;

            int oldSize = size;

            size = API.readMemoryInt(address + 0x50);
            int expected = (size == oldSize) ? offset : size * 16;

            if (size < 0 || size > 1024) return;
            if (super.pairs.length != size) super.pairs = Arrays.copyOf(super.pairs, size);

            long table = API.readMemoryLong(address + 0x48) & ByteUtils.ATOM_MASK;

            String key = null;
            offset = 8;
            for (int i = 0; offset < 8192 && i < size; offset += 8) {
                if (key == null && (key = getKey(readLong(table, offset, expected))) == null) continue;

                long value = readLong(table, offset += 8, expected);
                if (isInvalid(value)) continue;

                if (super.pairs[i] == null) super.pairs[i] = new Pair(key, value & ByteUtils.ATOM_MASK);
                else super.pairs[i++].set(key, value & ByteUtils.ATOM_MASK);
                key = null;
            }
        }

    }

    private static class Dictionary extends PairArray {
        private String[] removed;
        private static final byte[] BUFFER = new byte[4096];

        @Override
        public void update() {
            if (address == 0) return;

            long tableInfo = API.readMemoryLong(address + 32);

            if (tableInfo == 0) return;

            int  size   = API.readMemoryInt(tableInfo + 16);
            long table  = align8(API.readMemoryLong(tableInfo + 8));
            int  exp    = API.readMemoryInt(tableInfo + 20);
            int  length = (int) (Math.pow(2, exp) * 4);

            if (length > 4096 || length < 0 || size < 0 || size > 1024) return;

            if (super.pairs.length < size) {
                removed = new String[size];
                super.pairs = Arrays.copyOf(super.pairs, size);
            }

            API.readMemory(table, BUFFER, length);

            int current = 0, remove = 0;
            for (int offset = 0; offset < length && current < size; offset += 16) {
                long keyAddr = ByteUtils.getLong(BUFFER, offset) - 2, valAddr = ByteUtils.getLong(BUFFER, offset + 8) - 1;

                if (keyAddr == -2 || (valAddr >= -2 && valAddr <= 9)) continue;

                Pair pair = super.pairs[current];
                if (pair == null) super.pairs[current] = new Pair(API.readMemoryString(keyAddr), valAddr);
                else if (pair.value != valAddr) {
                    removed[remove++] = pair.key;
                    super.pairs[current].set(API.readMemoryString(keyAddr), valAddr);
                }
                current++;
            }

            while (this.size > current) {
                removed[remove++] = super.pairs[--this.size].key;
                super.pairs[this.size] = null;
            }
            this.size = current;

            for (String str; remove > 0;) {
                if (hasKey(str = removed[--remove])) continue;
                Lazy<Long> l = super.lazy.get(str);
                if (l != null) l.send(0L);
            }
        }

        private long align8(long value) {
            long aligned = value + 8 - (value & 0b1111);
            return aligned <= value ? aligned + 8 : aligned;
        }
    }

    public class Pair {
        public String key;
        public long value;

        private Pair() {}
        private Pair(String key, long value) {
            set(key, value);
        }


        private void set(String index, long value) {
            this.key   = index;
            this.value = value;

            Lazy<Long> l = lazy.get(index);
            if (l != null) l.send(value);
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "key='" + key + '\'' +
                    ", value=" + value +
                    '}';
        }
    }
}
