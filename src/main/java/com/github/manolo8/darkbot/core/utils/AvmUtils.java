package com.github.manolo8.darkbot.core.utils;

import static com.github.manolo8.darkbot.Main.API;

/**
 * Memory layout of the AVM2 structures the bot reads, and small helpers over them.
 * <p>
 * These are the Flash Player's own structures, not the game's: they don't change with client updates.
 */
public final class AvmUtils {

    /** Script object: its vtable. */
    public static final int OBJECT_VTABLE = 0x10;
    /** VTable: the traits of its class. */
    public static final int VTABLE_TRAITS = 0x28;
    /** Traits: the VM core of its class, the same for every class of one client. */
    public static final int TRAITS_CORE = 0x08;

    /** Array: its length and its dense atom list. */
    public static final int ARRAY_LENGTH = 0x28, ARRAY_ATOMS = 0x20;
    /** Vector: its length and its atom list. */
    public static final int VECTOR_LENGTH = 0x38, VECTOR_ATOMS = 0x30;
    /** Atom list of an Array or a Vector: where its elements start. */
    public static final int ATOMS_FIRST = 0x10;

    private AvmUtils() {}

    public static long vtableOf(long object) {
        return API.readLong(object + OBJECT_VTABLE);
    }

    /** The VM core of the object's class, which identifies the client: 0 if the object is null or not readable. */
    public static long vmCore(long object) {
        return object == 0 ? 0 : API.readLong(object, OBJECT_VTABLE, VTABLE_TRAITS, TRAITS_CORE);
    }

    public static int arrayLength(long array) {
        return API.readInt(array + ARRAY_LENGTH);
    }

    public static long arrayAtoms(long array) {
        return API.readLong(array + ARRAY_ATOMS);
    }

    /** The object at the index of an atom list, or 0 for null or a hole. The caller checks the index. */
    public static long atomAt(long atoms, int index) {
        return API.readLong(atoms + ATOMS_FIRST + (long) index * Long.BYTES) & ByteUtils.ATOM_MASK;
    }
}
