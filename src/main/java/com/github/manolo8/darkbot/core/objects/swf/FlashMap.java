package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.NativeUpdatable;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.core.utils.FilteredList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.manolo8.darkbot.Main.API;

/**
 * Object[K] = V, Array[K] = V, Dictionary[K] = V
 */
public class FlashMap<K, V> extends AbstractMap<K, V> implements NativeUpdatable {
    /**
     * since identifiers are always interned strings, they can't be 0,
     * so we can use 0 as the empty value.
     */
    public static final int EMPTY_ITEM = 0x0;

    // DELETED is stored as the key for deleted items
    public static final int DELETED_ITEM = 0x4;

    private static final int DONT_ENUM_BIT = 0x01;
    private static final int HAS_DELETED_ITEMS = 0x02;
    private static final int HAS_ITER_INDEX = 0x04;

    public static final int DICTIONARY_FLAG = 1 << 4;

    public static final int MAX_SIZE = 2048;
    public static final int MAX_CAPACITY = MAX_SIZE * Long.BYTES * 2;

    private final AtomKind keyKind;
    private final AtomKind valueKind;

    //private final Class<K> keyType;
    private final Supplier<V> valueConstructor;
    private final Class<V> valueType;

    private final boolean valueUpdatable;

    private int size;
    private Entry[] entries;

    private long address;
    private EntrySet entrySet;
    private Map<K, UpdatableWrapper> updatables;
    private boolean threadSafe, autoUpdate = true;

    // a read-only view into the values
    @Getter(lazy = true)
    private final List<V> valueList = new ValuesList();

    private class ValuesList extends AbstractList<V> implements RandomAccess {
        @Override
        public V get(int index) {
            Objects.checkIndex(index, size);
            return entries[index].getValue();
        }

        @Override
        public int size() {
            return size;
        }
    }

    private FlashMap(Class<K> keyType, Class<V> valueType) {
        if (keyType == null || valueType == null) {
            this.keyKind = null;
            this.valueKind = null;

            //this.keyType = null;
            this.valueType = null;
            this.valueUpdatable = false;
            this.valueConstructor = null;
        } else {
            this.keyKind = AtomKind.of(keyType);
            this.valueKind = AtomKind.of(valueType);

            //this.keyType = keyType;
            this.valueType = valueType;
            if (keyKind.isNotSupported() || valueKind.isNotSupported())
                throw new UnsupportedOperationException("Provided java types are not supported!");
            if (Updatable.class.isAssignableFrom(keyType))
                throw new UnsupportedOperationException("Key as updatable is not supported!");

            this.valueUpdatable = Updatable.class.isAssignableFrom(valueType);
            if (valueUpdatable) {
                //noinspection unchecked
                Class<V> constructorClass = valueType == Updatable.class ? (Class<V>) Updatable.NoOp.class : valueType;
                this.valueConstructor = () -> Main.INSTANCE.pluginAPI.requireInstance(constructorClass);
            } else this.valueConstructor = null;
        }

        clearMap();
    }

    public static boolean hasHashMap(long address) {
        int tableOffset = API.readInt(address, 16, 40, 236);
        return tableOffset != 0 && API.readLong(address + tableOffset) != 0;
    }

    public static <K, V> FlashMap<K, V> of(Class<K> keyType, Class<V> valueType) {
        return new FlashMap<>(keyType, valueType);
    }

    public static FlashMap<Object, Object> ofUnknown() {
        return new FlashMap<>(null, null);
    }

    private static void updateIfChanged(Updatable u, long address) {
        if (u.address != address)
            u.update(address);
    }

    public FlashMap<K, V> makeThreadSafe() {
        this.threadSafe = true;
        return this;
    }

    public FlashMap<K, V> noAuto() {
        this.autoUpdate = false;
        return this;
    }

    public List<V> asFiltered(Predicate<V> filter) {
        return new FilteredList<>(getValueList(), filter);
    }

    public void update() {
        if (address == 0) return;

        long traits = readLong(16, 40);

        int tableOffset = API.readInt(traits, 236);
        if (tableOffset == 0) return;

        boolean isDictionary = (API.readInt(traits, 248) & DICTIONARY_FLAG) != 0;
        if (isDictionary) {
            readHashTable(readLong(tableOffset) + 8); //read hash table ptr & skip cpp vtable
        } else {
            readHashTable(address + tableOffset);
        }
    }

    public void update(long address) {
        if (this.address != address) clearMap();
        this.address = address;
        if (autoUpdate) update();
    }

    private int getCapacity(int logCapacity, @SuppressWarnings("unused") boolean hasIterIndexes) {
        if (logCapacity <= 0) return 0;

        // Math.pow(2, logCapacity - 1)
        logCapacity = 1 << (logCapacity - 1);
        //if (hasIterIndexes) logCapacity += 2; // for tests only

        return logCapacity * Long.BYTES;
    }

    private long getAtom(long atoms, int offset) {
        return API.readLong(atoms + offset);
    }

    private void readHashTable(long table) {
        long atomsAndFlags = API.readLong(table);
        long atoms = (atomsAndFlags & ByteUtils.ATOM_MASK) + 8;

        int size = API.readInt(table, 8); // includes deleted items
        int capacity = getCapacity(API.readInt(table, 12), (atomsAndFlags & HAS_ITER_INDEX) != 0);

        //noinspection unused
        boolean hasDeletedItems = (atomsAndFlags & HAS_DELETED_ITEMS) != 0;

        if (size <= 0 || size > MAX_SIZE || capacity <= 0 || capacity > MAX_CAPACITY) {
            setSize(0);
            resetUpdatablesIfNoRef();
            return;
        }
        if (entries.length < size) {
            entries = Arrays.copyOf(entries, (int) Math.min(size * 1.25, MAX_SIZE));
        }

        int currentSize = 0, realSize = 0;
        for (int offset = 0; offset < capacity && currentSize < size; offset += 8) {
            long keyAtom = getAtom(atoms, offset);

            if (keyAtom == EMPTY_ITEM) continue;
            if (keyAtom == DELETED_ITEM) {
                offset += 8;
                currentSize++;
                continue; // skip deleted pair
            }

            AtomKind keyKind = AtomKind.of(keyAtom);

            // if keyKind is Double, most-likely it is a weak key
            if (keyKind == AtomKind.DOUBLE) {
                keyKind = AtomKind.OBJECT;

                // it is no more an atom tagged pointer
                keyAtom = API.readLong(keyAtom & ByteUtils.ATOM_MASK);
            }
            if (this.keyKind != null && keyKind != this.keyKind) {
                System.out.println("Invalid keyKind! expected: " + this.keyKind + ", read: " + keyKind);
                break;
            }

            long valueAtom = getAtom(atoms, (offset += 8));
            AtomKind valueKind = AtomKind.of(valueAtom);
            if (this.valueKind != null && valueKind != this.valueKind) {
                System.out.println("Invalid valueKind! expected: " + this.valueKind + ", read: " + valueKind);
                break;
            }

            Entry entry = entries[realSize];
            if (entry == null)
                entry = entries[realSize] = new Entry();

            entry.set(keyAtom, valueAtom, keyKind, valueKind);

            realSize++;
            currentSize++;
        }

        setSize(realSize);
        resetUpdatablesIfNoRef();
    }

    private void clearMap() {
        setSize(0);
        //noinspection unchecked
        this.entries = (Entry[]) Array.newInstance(Entry.class, 0);
        if (updatables != null)
            updatables.forEach((k, wrapper) -> wrapper.resetReferences());
    }

    public <T extends Updatable> T putUpdatable(K key, T updatable) {
        if (valueType == null || !valueType.isInstance(updatable))
            throw new IllegalArgumentException("value type is unknown or given updatable is not instance of value type!");
        if (updatables == null) updatables = new HashMap<>();
        updatables.put(key, new UpdatableWrapper(updatable));

        return updatable;
    }

    public Updatable removeUpdatable(K key) {
        if (updatables == null) return null;
        UpdatableWrapper v = updatables.remove(key);
        return v == null ? null : v.value;
    }

    private int indexOf(Object key) {
        for (int i = 0; i < size(); i++) {
            Entry entry = entries[i];
            if (entry.getKey() == null) continue;
            if (entry.getKey().equals(key)) return i;
        }

        return -1;
    }

    private void setSize(int realSize) {
        for (int i = realSize; i < size(); i++) {
            Entry entry = entries[i];
            if (entry != null) entry.reset();
        }
        this.size = realSize;
    }

    private void resetUpdatablesIfNoRef() {
        if (updatables != null)
            updatables.forEach((k, wrapper) -> wrapper.resetIfNoReferences());
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return entrySet == null ? entrySet = new EntrySet() : entrySet;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public V get(Object key) {
        int i = indexOf(key);
        return i == -1 ? null : entries[i].getValue();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        int i = indexOf(key);
        return i == -1 ? defaultValue : entries[i].getValue();
    }

    public V getOrWrapper(K key) {
        if (updatables != null) {
            UpdatableWrapper updatableWrapper = updatables.get(key);
            if (updatableWrapper != null)
                //noinspection unchecked
                return (V) updatableWrapper.value;
        }
        return get(key);
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "FlashMap[" + size() + "]";
    }

    private static class UpdatableWrapper {
        private final Updatable value;

        private int references;

        private UpdatableWrapper(Updatable value) {
            this.value = value;
        }

        private void resetIfNoReferences() {
            if (references <= 0)
                updateIfChanged(value, 0);
        }

        private void resetReferences() {
            references = 0;
            updateIfChanged(value, 0);
        }

        private void removeReference() {
            references--;
        }

        private void addReference() {
            references++;
        }
    }

    private class Entry implements Map.Entry<K, V> {
        private long keyAtomCache, valueAtomCache;

        @Getter
        private K key;
        private V value;

        private UpdatableWrapper wrapper;

        public Entry() {
            if (valueConstructor != null)
                this.value = valueConstructor.get();
        }

        private void set(long keyAtom, long valueAtom, AtomKind keyKind, AtomKind valueKind) {
            boolean keyChanged = keyAtomCache != keyAtom;
            if (keyChanged) {
                setKey(keyKind.readAtom(keyAtom, threadSafe));
                keyAtomCache = keyAtom;
            }

            if (keyChanged || (valueAtomCache != valueAtom)) {
                setVal(valueKind.readAtom(valueAtom, threadSafe));
                valueAtomCache = valueAtom;
            }

            if (wrapper != null) {
                wrapper.value.update();
            } else if (value instanceof Updatable) {
                ((Updatable) value).update();
            }
        }

        private void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            //noinspection unchecked
            return wrapper == null ? value : (V) wrapper.value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("FlashEntry#setValue");
        }

        private void setVal(V value) {
            if (valueUpdatable) {
                UpdatableWrapper v = updatables == null ? null : updatables.get(key);
                if (v != null) {
                    if (v != wrapper) {
                        if (wrapper != null)
                            wrapper.removeReference();

                        v.addReference();
                        wrapper = v;
                    }
                } else if (wrapper != null) {
                    this.wrapper.removeReference();
                    this.wrapper = null;
                }
            }

            if (wrapper != null) {
                updateIfChanged(this.wrapper.value, (long) value);
            } else if (this.value instanceof Updatable) {
                updateIfChanged((Updatable) this.value, (long) value);
            } else {
                this.value = value;
            }
        }

        private void reset() {
            if (value instanceof Updatable)
                updateIfChanged((Updatable) value, 0);

            if (wrapper != null) {
                this.wrapper.removeReference();
                this.wrapper = null;
            }
        }

        @Override
        public String toString() {
            return "Entry{" + "key=" + key + ", value=" + getValue() + '}';
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
            if (!hasNext())
                throw new NoSuchElementException("No more elements in the iterator");
            return FlashMap.this.entries[nextIdx++];
        }
    }
}