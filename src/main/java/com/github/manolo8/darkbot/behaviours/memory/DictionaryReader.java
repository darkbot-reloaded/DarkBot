package com.github.manolo8.darkbot.behaviours.memory;

import com.github.manolo8.darkbot.core.objects.swf.FlashListLong;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.behaviours.memory.AvmMemory.*;

/**
 * Reads the entries of an AS3 Dictionary with object keys.
 * <p>
 * Unlike {@link FlashListLong#ofMapKeys()}, a dictionary that can't be read is told apart from an empty one, and
 * weak keys are resolved to the object they point to. A cleaner must never take an unreadable dictionary for an empty
 * one: "no live objects" would make every object look like garbage.
 */
final class DictionaryReader {

    /** Atoms (keys and values) of the largest hash table read. The dictionaries of the cleaners stay far below. */
    private static final int MAX_CAPACITY = 1 << 16;
    /** A capacity is stored as log2 + 1, so this is the largest value {@link #MAX_CAPACITY} allows. */
    private static final int MAX_LOG_CAPACITY = Integer.numberOfTrailingZeros(MAX_CAPACITY) + 1;

    private final WordBuffer atoms = new WordBuffer();
    private long[] keys = new long[0], values = new long[0];
    private int size;

    /**
     * Reads the entries of the dictionary. False if it can't be read: not a dictionary, keys that are not objects,
     * a table too large, or changed by the game while it was read, in which case the next pass reads it again.
     */
    boolean read(long dictionary) {
        size = 0;
        if (dictionary == 0) return false;
        long traits = API.readLong(dictionary, OBJECT_VTABLE, VTABLE_TRAITS);
        int tableOffset = API.readInt(traits + TRAITS_HASHTABLE_OFFSET);
        if (tableOffset <= 0) return false;
        boolean isDictionary = (API.readInt(traits + TRAITS_FLAGS) & TRAITS_FLAG_DICTIONARY) != 0;
        long table = isDictionary
                ? API.readLong(dictionary + tableOffset) + DICTIONARY_TABLE_SKIP
                : dictionary + tableOffset;

        long atomList = API.readLong(table + HASHTABLE_ATOMS) & ByteUtils.ATOM_MASK;
        int used = API.readInt(table + HASHTABLE_USED);
        int logCapacity = API.readInt(table + HASHTABLE_LOG_CAPACITY);
        if (used == 0) return true;
        if (atomList == 0 || logCapacity <= 0 || logCapacity > MAX_LOG_CAPACITY) return false;
        int capacity = 1 << (logCapacity - 1);
        if (used < 0 || used > capacity / 2) return false;

        atoms.read(atomList + ATOMS_FIRST, capacity);
        if (!collect(capacity, used)) return fail();
        // Unchanged table and count: the game didn't rehash or add entries while it was read
        if ((API.readLong(table + HASHTABLE_ATOMS) & ByteUtils.ATOM_MASK) == atomList
                && API.readInt(table + HASHTABLE_USED) == used) return true;
        return fail();
    }

    /** Collects the live entries of the read atoms. False if a key is not an object or there are too many. */
    private boolean collect(int capacity, int used) {
        if (keys.length < used) {
            keys = new long[used];
            values = new long[used];
        }
        for (int i = 0; i < capacity; i += 2) {
            long key = atoms.get(i);
            if (key == KEY_EMPTY || key == KEY_DELETED) continue;
            if (size == used) return false;

            long object;
            int tag = atomTag(key);
            if (tag == ATOM_OBJECT) object = key & ByteUtils.ATOM_MASK;
            else if (tag == ATOM_WEAK_KEY) object = API.readLong(key & ByteUtils.ATOM_MASK);
            else return false;
            if (object == 0) continue; // weak key already collected

            keys[size] = object;
            values[size++] = atoms.get(i + 1) & ByteUtils.ATOM_MASK;
        }
        return true;
    }

    private boolean fail() {
        size = 0;
        return false;
    }

    int size() {
        return size;
    }

    long key(int index) {
        return keys[index];
    }

    long value(int index) {
        return values[index];
    }

    /** Frees the buffers, e.g. after a client reload. */
    void release() {
        atoms.release();
        keys = values = new long[0];
        size = 0;
    }
}
