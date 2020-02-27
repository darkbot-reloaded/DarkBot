package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.Lazy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.github.manolo8.darkbot.Main.API;

public class EntryArray extends Updatable {
    public static final long FIX = 0xfffffffffff8L;

    private Map<String, Lazy<Long>> lazy = new HashMap<>();

    public Entry[] entries;
    public int size;

    public EntryArray() {
        this.entries = new Entry[0];
    }

    public void addLazy(String key, Consumer<Long> consumer) {
        this.lazy.computeIfAbsent(key, k -> new Lazy<>()).add(consumer);
    }

    @Override
    public void update() {
        size = API.readMemoryInt(address + 0x50);

        if (size < 0 || size > 2048) return;
        if (entries.length < size) entries = new Entry[Math.min((int) (size * 1.25), 2048)];

        long index = 0;
        long table = (API.readMemoryLong(address + 0x48) & FIX) + 0x8;
        for (int offset = 0, i = 0; offset < 8192 && i < size; offset += 8) {
            if (index == 0) {
                index = API.readMemoryLong(table + offset) & FIX;
                offset += 8;
            }
            long value = API.readMemoryLong(table + offset);
            if (index == 0 || value == 0) continue;

            if (entries[i] == null) entries[i] = new Entry();
            entries[i++].set(API.readMemoryString(index), value & FIX);
            index = 0;
        }
    }

    private class Entry {
        public String index;
        public long value;

        private void set(String index, long value) {
            this.index = index;
            this.value = value;

            Lazy<Long> l = lazy.get(index);
            if (l != null) l.send(value);
        }
    }
}