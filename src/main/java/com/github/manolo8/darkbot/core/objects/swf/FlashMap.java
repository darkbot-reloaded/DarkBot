package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.github.manolo8.darkbot.Main.API;

/**
 * Object[K] = V, Array[K] = V, Dictionary[K] = V
 */
public class FlashMap<K, V> extends AbstractMap<K, V> implements IUpdatable {
    /**
     * since identifiers are always interned strings, they can't be 0,
     * so we can use 0 as the empty value.
     */
    private static final int EMPTY = 0x0;

    // DELETED is stored as the key for deleted items
    private static final int DELETED = 0x4;

    private static final int kDontEnumBit = 0x01;
    private static final int kHasDeletedItems = 0x02;
    private static final int kHasIterIndex = 0x04;

    private static final int MAX_SIZE = 1024;
    private static final int MAX_CAPACITY = MAX_SIZE * Long.BYTES * 2;
    private static final byte[] BUFFER = new byte[MAX_CAPACITY];

    private final AtomKind keyKind;
    private final AtomKind valueKind;

    private final Class<K> keyType;
    private final Class<V> valueType;

    private final boolean keyUpdatable, valueUpdatable;

    private int size;
    private Entry[] entries;

    private long address;
    private Map<K, ValueWrapper<V>> updatable;

    public FlashMap(Class<K> keyType, Class<V> valueType) {
        this.keyKind = AtomKind.of(keyType);
        this.valueKind = AtomKind.of(valueType);

        this.keyType = keyType;
        this.valueType = valueType;
        if (keyKind.isNotSupported() || valueKind.isNotSupported())
            throw new IllegalArgumentException("Illegal java types!");

        this.keyUpdatable = Updatable.class.isAssignableFrom(keyType);
        this.valueUpdatable = Updatable.class.isAssignableFrom(valueType);

        if (keyUpdatable)
            throw new IllegalArgumentException("Key as updatable is not supported!");

        clear();
    }

    @Override
    public void clear() {
        this.size = 0;
        //noinspection unchecked
        this.entries = (Entry[]) Array.newInstance(Entry.class, 0);
        if (updatable != null) updatable.forEach((k, wrapper) -> {
            wrapper.references = 0;
            if (wrapper.value instanceof Updatable) {
                Updatable u = (Updatable) wrapper.value;
                if (u.address != 0)
                    u.update(0);
            }
        });
    }

    @Override
    public void update() {
        if (address == 0) return;

        long traits = API.readLong(address, 16, 40);

        int tableOffset = API.readInt(traits, 236);
        if (tableOffset == 0) return;

        boolean isDictionary = (API.readInt(traits, 248) & (1 << 4)) != 0;
        if (isDictionary) {
            readHashTable(API.readLong(address + tableOffset) + 8); //read hash table ptr & skip cpp vtable
        } else {
            readHashTable(address + tableOffset);
        }
    }

    @Override
    public void update(long address) {
        if (this.address != address) {
            clear();
        }

        this.address = address;
    }

    private void readHashTable(long table) {
        long atomsAndFlags = API.readLong(table);
        long atoms = (atomsAndFlags & ByteUtils.ATOM_MASK) + 8;

        int size = API.readMemoryInt(table + 8); // includes deleted items
        int capacity = getCapacity(API.readMemoryInt(table + 12), (atomsAndFlags & kHasIterIndex) != 0);

        if (size <= 0 || size > MAX_SIZE || capacity <= 0 || capacity > MAX_CAPACITY) return;
        if (entries.length < size) entries = Arrays.copyOf(entries, size);

        API.readMemory(atoms, BUFFER, capacity); //TODO add readLongs in API
        int currentSize = 0, realSize = 0;
        for (int offset = 0; offset < capacity && currentSize < size; offset += 8) {
            long keyAtom = ByteUtils.getLong(BUFFER, offset);

            if (keyAtom == EMPTY) continue;
            if (keyAtom == DELETED) {
                offset += 8;
                currentSize++;
                continue; // skip deleted pair
            }

            AtomKind keyKind = AtomKind.of(keyAtom);
            if (keyKind != this.keyKind) {
                System.out.println("Invalid keyKind! expected: " + this.keyKind + ", read: " + keyKind);
                break;
            }

            long valueAtom = ByteUtils.getLong(BUFFER, (offset += 8));
            AtomKind valueKind = AtomKind.of(valueAtom);
            if (valueKind != this.valueKind) {
                System.out.println("Invalid valueKind! expected: " + this.valueKind + ", read: " + valueKind);
                break;
            }

            Entry entry = entries[realSize];
            if (entry == null) entry = entries[realSize] = new Entry();

            entry.set(keyAtom, valueAtom);

            realSize++;
            currentSize++;
        }

        for (int i = realSize; i < this.size; i++) {
            Entry entry = entries[i];
            if (entry != null) entry.reset();
        }

        if (updatable != null)
            updatable.forEach((k, wrapper) -> {
                //todo extract to method
                if (wrapper.references <= 0 && wrapper.value instanceof Updatable) {
                    Updatable u = (Updatable) wrapper.value;
                    if (u.address != 0)
                        u.update(0);
                }
            });

        this.size = realSize;
    }

    private int getCapacity(int logCapacity, boolean hasIterIndexes) {
        if (logCapacity <= 0) return 0;

        logCapacity = 1 << (logCapacity - 1);
        //if (hasIterIndexes) logCapacity += 2; // for tests only

        return logCapacity * Long.BYTES;
    }

    @NotNull
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public V get(Object key) {
        int i = indexOf(key);
        return i == -1 ? null : entries[i].value;
    }

    @Override
    public V put(K key, V value) {
        if (valueKind != AtomKind.OBJECT) return null; //?
        if (updatable == null) updatable = new HashMap<>();
        updatable.put(key, new ValueWrapper<>(value));

        return value;
    }

    @Override
    public V remove(Object key) {
        if (updatable == null) return null;
        ValueWrapper<V> v = updatable.remove(key);
        return v == null ? null : v.value;
    }

    private int indexOf(Object key) {
        for (int i = 0; i < size(); i++) {
            Entry entry = entries[i];
            if (entry == null || entry.getKey() == null) continue;
            if (entry.key.equals(key)) return i;
        }

        return -1;
    }

    private class Entry implements Map.Entry<K, V> {
        private long keyAtomCache, valueAtomCache;

        private K key;
        private V value;

        private ValueWrapper<V> wrapper;

        public Entry() {
            if (keyUpdatable)
                this.key = HeroManager.instance.main.pluginAPI.requireInstance(keyType);
            if (valueUpdatable)
                this.value = HeroManager.instance.main.pluginAPI.requireInstance(valueType);
        }

        private void set(long keyAtom, long valueAtom) {
            boolean keyChanged = keyAtomCache != keyAtom;
            if (keyChanged) {
                setKey(keyKind.read(keyAtom));
                keyAtomCache = keyAtom;
            }

            if (keyChanged || (valueAtomCache != valueAtom)) {
                setVal(valueKind.read(valueAtom));
                valueAtomCache = valueAtom;
            }

            if (key instanceof Updatable)
                ((Updatable) key).update();

            if (value instanceof Updatable)
                ((Updatable) value).update();
        }

        public K getKey() {
            return key;
        }

        private void setKey(K key) {
            if (this.key instanceof Updatable) {
                ((Updatable) this.key).update((long) key);
            } else {
                this.key = key;
            }
        }

        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("FlashEntry#setValue");
        }

        private void setVal(V value) {
            if (keyKind != AtomKind.OBJECT && valueKind == AtomKind.OBJECT) {

                ValueWrapper<V> v = updatable == null ? null : updatable.get(key);
                if (v != null && this.value != v) {
                    this.value = v.value;
                    this.wrapper = v;
                    v.references++;
                } else if (wrapper != null) {
                    this.wrapper.references--;
                    this.wrapper = null;
                    this.value = HeroManager.instance.main.pluginAPI.requireInstance(valueType);
                }
            }

            if (this.value instanceof Updatable) {
                ((Updatable) this.value).update((long) value);
            } else {
                this.value = value;
            }
        }

        private void reset() {
            if (key instanceof Updatable) {
                Updatable u = (Updatable) key;
                if (u.address != 0)
                    u.update(0);
            }

            if (value instanceof Updatable) {
                Updatable u = (Updatable) value;
                if (u.address != 0)
                    u.update(0);
            }
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }
    }

    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        @Override
        public @NotNull Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public int size() {
            return size;
        }
    }

    private class EntryIterator implements Iterator<Map.Entry<K, V>> {
        private int nextIdx;

        @Override
        public boolean hasNext() {
            return nextIdx < FlashMap.this.size;
        }

        @Override
        public Map.Entry<K, V> next() {
            return FlashMap.this.entries[nextIdx++];
        }
    }

    private static class ValueWrapper<V> {
        private final V value;

        private int references;

        public ValueWrapper(V value) {
            this.value = value;
        }
    }
}
