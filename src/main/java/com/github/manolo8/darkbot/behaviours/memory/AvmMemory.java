package com.github.manolo8.darkbot.behaviours.memory;

import com.github.manolo8.darkbot.core.utils.ByteUtils;

import static com.github.manolo8.darkbot.Main.API;

/**
 * Memory layout of the AVM2 structures the cleaners read, in one place.
 * <p>
 * These are the Flash Player's own structures, not the game's: they don't change with client updates.
 * Game fields are never addressed by offset, they are resolved by name with {@link SlotResolver}.
 */
final class AvmMemory {

    /** ScriptObject: its vtable. */
    static final int OBJECT_VTABLE = 0x10;
    /** VTable: the traits of its class. */
    static final int VTABLE_TRAITS = 0x28;
    /** Traits: the VM core the class belongs to. */
    static final int TRAITS_CORE = 0x08;
    /** Traits: offset of the property hash table in instances of dynamic classes, 0 if not dynamic. */
    static final int TRAITS_HASHTABLE_OFFSET = 0xec;
    /** Traits: size of an instance, i.e. where its fields end. */
    static final int TRAITS_TOTAL_SIZE = 0xf0;
    /** Traits: class flags. */
    static final int TRAITS_FLAGS = 0xf8;
    /** {@link #TRAITS_FLAGS} bit of Dictionary, whose hash table is a separate object. */
    static final int TRAITS_FLAG_DICTIONARY = 1 << 4;

    /** Dictionary: its hash table object starts with a C++ vtable. */
    static final int DICTIONARY_TABLE_SKIP = 0x08;
    /** HeapHashtable: atoms pointer (low bits are flags), used entries including deleted ones, log2 capacity + 1. */
    static final int HASHTABLE_ATOMS = 0x00, HASHTABLE_USED = 0x08, HASHTABLE_LOG_CAPACITY = 0x0c;
    /** Atom list of a hash table: entries start after its header. */
    static final int ATOMS_FIRST = 0x08;
    /** Hash table keys of empty and deleted entries. */
    static final long KEY_EMPTY = 0x0, KEY_DELETED = 0x4;

    /** Array: length, dense atom list, and where elements start in that list. */
    static final int ARRAY_LENGTH = 0x28, ARRAY_ATOMS = 0x20, ARRAY_ATOMS_FIRST = 0x10;
    /** Vector of objects: length, atom list, and where elements start in that list. */
    static final int VECTOR_LENGTH = 0x38, VECTOR_ATOMS = 0x30, VECTOR_ATOMS_FIRST = 0x10;

    /** Atom tag of an object. */
    static final int ATOM_OBJECT = ByteUtils.OBJECT_TYPE;
    /** Atom tag of a weak Dictionary key: a GCWeakRef, whose first word is the object, tagged as a double. */
    static final int ATOM_WEAK_KEY = 7;

    private AvmMemory() {}

    /** The VM core of the object's class, or 0 if the object is null or its class can't be read. */
    static long coreOf(long object) {
        return object == 0 ? 0 : API.readLong(object, OBJECT_VTABLE, VTABLE_TRAITS, TRAITS_CORE);
    }

    static int atomTag(long atom) {
        return (int) (atom & ByteUtils.ATOM_KIND);
    }
}
