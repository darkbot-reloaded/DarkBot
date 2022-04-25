package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.core.api.util.DataBuffer;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;

import static com.github.manolo8.darkbot.Main.API;

// tableOffset = API.readInt(address, 0x10, 0x28, 236);
// isDictionary = (API.readInt(address, 0x10, 0x28, 248) & (1 << 4)) != 0; // need to read heap hashtable
@Deprecated // TODO unfished, dont use it
public class FlashMap<K, V> extends SwfPtrCollection {

    private final AtomKind keyKind;
    private final AtomKind valueKind;

    private final boolean keyUpdatable, valueUpdatable;
    private final Class<K> keyClazz;
    private final Class<V> valueClazz;

    private int size;
    private boolean autoUpdatable;

    @SuppressWarnings("unchecked")
    private Entry[] entries = (Entry[]) Array.newInstance(Entry.class, 0);

    private FlashMap(Class<K> key, Class<V> value) {
        this.keyClazz = key;
        this.valueClazz = value;

        this.keyUpdatable = Updatable.class.isAssignableFrom(keyClazz);
        this.valueUpdatable = Updatable.class.isAssignableFrom(valueClazz);

        this.keyKind = AtomKind.of(keyClazz);
        this.valueKind = AtomKind.of(valueClazz);

        if (keyKind == null || keyKind == AtomKind.UNUSED || valueKind == null || valueKind == AtomKind.UNUSED)
            throw new IllegalArgumentException("Provided types are not supported");
    }

    public static <K, V> FlashMap<K, V> of(Class<K> key, Class<V> value) {
        return new FlashMap<K, V>(key, value);
    }

    public FlashMap<K, V> setAutoUpdatable(boolean updatable) {
        this.autoUpdatable = updatable;
        return this;
    }

    @Override
    public void update() {
        if (address == 0) return;

        // heap hashtable
        boolean isDictionary = (API.readInt(address, 0x10, 0x28, 248) & (1 << 4)) != 0;
        int tableOffset = API.readInt(address, 0x10, 0x28, 236);

        long table = address + tableOffset;
        if (isDictionary) table = API.readLong(table) + 8;

        if (table == 0) return;

        long atoms = API.readLong(table);
        long sizeAndExp = API.readLong(table + 8);

        //boolean hasIterIndex = (atoms & 0x04) != 0; //unused here

        int size = (int) sizeAndExp;
        int exp = (int) (sizeAndExp >> 32);

        int capacity = (1 << (exp - 1)) * Long.BYTES;

        if (size <= 0 || size > 1024 || capacity <= 0 || capacity > 1024 * 2 * 8) return;
        if (entries.length < size) entries = Arrays.copyOf(entries, size);

        this.size = size;

        atoms = (atoms & ByteUtils.ATOM_MASK) + 8; //remove tags & skip c++ vtable

        for (int offset = 0, idx = 0; offset < capacity && idx < size; offset += DataBuffer.MAX_CHUNK_SIZE) {
            try (DataBuffer r = API.readData(atoms + offset,
                    Math.min(DataBuffer.MAX_CHUNK_SIZE, capacity - offset))) {

                while (r.getAvailable() >= 16 && idx < size) {
                    long key = r.getLong();
                    if (AtomKind.isNullAtom(key)) continue; // key cannot be null

                    AtomKind keyKind = AtomKind.of(key);
                    if (keyKind != this.keyKind) continue;

                    Entry entry = entries[idx];
                    if (entry == null) entries[idx] = entry = new Entry();

                    entry.set(key, r.getLong());
                    idx++;
                }
            }
        }
    }

    @Override
    public void update(long address) {
        super.update(address);
        if (autoUpdatable) update();
    }

    @Override
    public int getSize() {
        return Math.min(size, entries.length);
    }

    @Override
    public long getPtr(int i) {
        if (i < getSize()) {
            Entry entry = entries[i];
            if (entry != null && entry.value instanceof Long)
                return (Long) entry.value;
        }
        return 0;
    }


    public class Entry implements Map.Entry<K, V> {
        private K key;
        private V value;

        private Entry() {
            try {
                if (keyUpdatable)
                    key = keyClazz.newInstance();
                if (valueUpdatable)
                    value = valueClazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            return null;
        }

        @SuppressWarnings("unchecked")
        private void set(long keyAtom, long valueAtom) {
            if (keyUpdatable) {
                ((Updatable) key).update(keyAtom & ByteUtils.ATOM_MASK);
                if (autoUpdatable) ((Updatable) key).update();
            } else key = (K) keyKind.read(keyAtom);

            if (valueUpdatable) {
                ((Updatable) value).update(valueAtom & ByteUtils.ATOM_MASK);
                if (autoUpdatable) ((Updatable) value).update();
            } else value = (V) valueKind.read(valueAtom);
        }

        @Override
        public String toString() {
            return "Entry{key=" + key + ", value=" + value + '}';
        }
    }
}
