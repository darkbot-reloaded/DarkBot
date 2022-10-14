package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private Map<K, ValueWrapper> updatables;

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

    private static void updateIfChanged(Updatable u, long address) {
        if (u.address != address)
            u.update(address);
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

    private int getCapacity(int logCapacity, @SuppressWarnings("unused") boolean hasIterIndexes) {
        if (logCapacity <= 0) return 0;

        logCapacity = 1 << (logCapacity - 1);
        //if (hasIterIndexes) logCapacity += 2; // for tests only

        return logCapacity * Long.BYTES;
    }

    private void readHashTable(long table) {
        long atomsAndFlags = API.readLong(table);
        long atoms = (atomsAndFlags & ByteUtils.ATOM_MASK) + 8;

        int size = API.readMemoryInt(table + 8); // includes deleted items
        int capacity = getCapacity(API.readMemoryInt(table + 12), (atomsAndFlags & kHasIterIndex) != 0);

        if (size <= 0 || size > MAX_SIZE || capacity <= 0 || capacity > MAX_CAPACITY) {
            this.size = 0;
            return;
        }
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
            if (entry == null)
                entry = entries[realSize] = new Entry();

            entry.set(keyAtom, valueAtom);

            realSize++;
            currentSize++;
        }

        for (int i = realSize; i < this.size; i++) {
            Entry entry = entries[i];
            if (entry != null) entry.reset();
        }

        if (updatables != null)
            updatables.forEach((k, wrapper) -> wrapper.resetIfNoReferences());

        this.size = realSize;
    }

    @Override
    public void clear() {
        this.size = 0;
        //noinspection unchecked
        this.entries = (Entry[]) Array.newInstance(Entry.class, 0);
        if (updatables != null)
            updatables.forEach((k, wrapper) -> wrapper.resetReferences());
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
        if (!valueUpdatable) throw new IllegalStateException("V Type must be Updatable!");
        if (updatables == null) updatables = new HashMap<>();
        updatables.put(key, new ValueWrapper(value));

        return value;
    }

    @Override
    public V remove(Object key) {
        if (updatables == null) return null;
        ValueWrapper v = updatables.remove(key);
        return v == null ? null : v.value;
    }

    private int indexOf(Object key) {
        for (int i = 0; i < size(); i++) {
            Entry entry = entries[i];
            if (entry.getKey() == null) continue;
            if (entry.key.equals(key)) return i;
        }

        return -1;
    }

    private class ValueWrapper {
        private final V value;

        private int references;

        private ValueWrapper(V value) {
            this.value = value;
        }

        private void resetIfNoReferences() {
            if (references <= 0)
                updateIfChanged((Updatable) value, 0);
        }

        private void resetReferences() {
            references = 0;
        }

        private @Nullable ValueWrapper removeReference() {
            references--;
            return null;
        }

        private @NotNull ValueWrapper addReference() {
            references++;
            return this;
        }
    }

    private class Entry implements Map.Entry<K, V> {
        private long keyAtomCache, valueAtomCache;

        private K key;
        private V value;

        private ValueWrapper wrapper;

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

            if (key instanceof Updatable) {
                ((Updatable) key).update();
            }

            if (wrapper != null) {
                ((Updatable) wrapper.value).update();
            } else if (value instanceof Updatable) {
                ((Updatable) value).update();
            }
        }

        public K getKey() {
            return key;
        }

        private void setKey(K key) {
            if (this.key instanceof Updatable)
                updateIfChanged((Updatable) this.key, (long) key);
            else this.key = key;
        }

        public V getValue() {
            return wrapper == null ? value : wrapper.value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("FlashEntry#setValue");
        }

        private void setVal(V value) {
            if (keyKind != AtomKind.OBJECT && valueKind == AtomKind.OBJECT) {

                ValueWrapper v = updatables == null ? null : updatables.get(key);
                if (v != null) {
                    if (v != wrapper) {
                        if (wrapper != null)
                            wrapper.removeReference();

                        wrapper = v.addReference();
                    }
                } else if (wrapper != null) {
                    this.wrapper = wrapper.removeReference();
                }
            }

            if (wrapper != null) {
                updateIfChanged((Updatable) this.wrapper.value, (long) value);
            } else if (value instanceof Updatable) {
                updateIfChanged((Updatable) this.value, (long) value);
            } else {
                this.value = value;
            }
        }

        private void reset() {
            if (key instanceof Updatable)
                updateIfChanged((Updatable) key, 0);

            if (value instanceof Updatable)
                updateIfChanged((Updatable) value, 0);

            if (wrapper != null) {
                wrapper = wrapper.removeReference();
            }
        }

        @Override
        public String toString() {
            return "Entry{" + "key=" + key + ", value=" + value + '}';
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
}
