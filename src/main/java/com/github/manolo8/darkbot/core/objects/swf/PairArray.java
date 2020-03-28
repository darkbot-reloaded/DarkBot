package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.core.utils.Lazy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.manolo8.darkbot.Main.API;

/**
 * Reads arrays with pair of values
 * Instead of EntryArray & Dictionary
 */
public class PairArray extends Updatable implements SwfPtrCollection {
    private final int sizeOffset, tableOffset;
    private final boolean isDictionary, autoUpdatable;

    public int size;

    private Pair[] pairs = new Pair[0];
    private Map<String, Lazy<Long>> lazy = new HashMap<>();

    protected PairArray(int sizeOffset, int tableOffset, boolean isDictionary, boolean autoUpdatable) {
        this.sizeOffset    = sizeOffset;
        this.tableOffset   = tableOffset;
        this.isDictionary  = isDictionary;
        this.autoUpdatable = autoUpdatable;
    }

    /**
     * Reads pairs of {@code Array}
     */
    public static PairArray ofArray() {
        return ofArray(false);
    }

    public static PairArray ofArray(boolean autoUpdatable) {
        return new PairArray(0x50, 0x48, false, autoUpdatable);
    }

    /**
     * Reads pairs of {@code Dictionary}
     */
    public static PairArray ofDictionary() {
        return ofDictionary(false);
    }

    public static PairArray ofDictionary(boolean autoUpdatable) {
        return new PairArray(0x10, 0x8, true, autoUpdatable);
    }

    public void addLazy(String key, Consumer<Long> consumer) {
        this.lazy.computeIfAbsent(key, k -> new Lazy<>()).add(consumer);
    }

    public int getSize() {
        return Math.min(size, pairs.length);
    }

    public long getPtr(int idx) {
        return get(idx).value;
    }

    public Pair get(int idx) {
        return idx >= 0 && idx < size && idx < pairs.length ? pairs[idx] : null;
    }

    public boolean hasKey(String key) {
        for (int i = 0; i < size && i < pairs.length; i++)
            if (pairs[i].key != null && pairs[i].key.equals(key)) return true;
        return false;
    }

    @Override
    public void update() {
        size = API.readMemoryInt(address + sizeOffset);

        if (size < 0 || size > 1024) return;
        if (pairs.length != size) pairs = Arrays.copyOf(pairs, size);

        long index = 0;
        long table = API.readMemoryLong(address + tableOffset) & ByteUtils.FIX;

        for (int offset = 8, i = 0; offset < 8192 && i < size; offset += 8) {
            if (isInvalid(index)) index = API.readMemoryLong(table + offset) & ByteUtils.FIX;
            if (isInvalid(index)) continue;

            long value = API.readMemoryLong(table + (offset += 8));
            if (isInvalid(value)) continue;

            if (pairs[i] == null) pairs[i] = new Pair();
            pairs[i++].set(API.readMemoryString(index), value & ByteUtils.FIX);
            index = 0;
        }

        if (isDictionary) resetMissingObj();
    }

    @Override
    public void update(long address) {
        super.update(isDictionary ? API.readMemoryLong(address + 0x20) : address);
        if (autoUpdatable) update();
    }

    private void resetMissingObj() {
        this.lazy.entrySet().parallelStream()
                .filter(l -> l.getValue() != null && l.getValue().value != null)
                .filter(l -> l.getValue().value != 0)
                .filter(l -> !hasKey(l.getKey()))
                .forEach(l -> l.getValue().send(0L));
    }

    private boolean isInvalid(long address) {
        return isDictionary ? address < 10 : address == 0;
    }

    public class Pair {
        public String key;
        public long value;

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
