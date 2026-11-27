package com.github.manolo8.darkbot.behaviours.memory;

import com.github.manolo8.darkbot.core.objects.swf.FlashListLong;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.behaviours.memory.AvmMemory.*;

/**
 * Reads the elements of an AS3 Array or Vector of objects, in bulk.
 * <p>
 * Unlike {@link FlashListLong}, a list that can't be read is told apart from an empty one, and the number of elements
 * read at once is chosen by the caller: a cleaner must never take an unreadable list for an empty one.
 */
final class ListReader {

    enum Layout {
        ARRAY(ARRAY_LENGTH, ARRAY_ATOMS, ARRAY_ATOMS_FIRST),
        VECTOR(VECTOR_LENGTH, VECTOR_ATOMS, VECTOR_ATOMS_FIRST);

        private final int length, atoms, first;

        Layout(int length, int atoms, int first) {
            this.length = length;
            this.atoms = atoms;
            this.first = first;
        }
    }

    private final Layout layout;
    private final int maxCount;
    private final WordBuffer atoms = new WordBuffer();
    /** Length of the list when last read, and how many of its elements were read: 0 if it couldn't be. */
    private int length, count;

    /** @param maxCount most elements read at once: the whole list for {@link #read}, a slice for {@link #readFrom} */
    ListReader(Layout layout, int maxCount) {
        this.layout = layout;
        this.maxCount = maxCount;
    }

    /** Reads all the elements of the list. False if it can't be read now, see {@link #readFrom}, or is too long. */
    boolean read(long list) {
        return readFrom(list, 0) && count == length;
    }

    /**
     * Reads the elements of the list from the index on, at most {@code maxCount} of them. False if it can't be read
     * now: null, shorter than the index, or changed by the game while it was read (pushed, spliced or moved), in
     * which case the next pass reads it again.
     */
    boolean readFrom(long list, int from) {
        count = length = 0;
        if (list == 0 || from < 0) return false;
        length = API.readInt(list + layout.length);
        long atomList = API.readLong(list + layout.atoms);
        if (length < from) return false;
        if (length == from) return true;
        if (atomList == 0) return false;

        int read = Math.min(length - from, maxCount);
        atoms.read(atomList + layout.first + (long) from * Long.BYTES, read);
        if (API.readInt(list + layout.length) != length || API.readLong(list + layout.atoms) != atomList) return false;
        count = read;
        return true;
    }

    /** The object at the index of the list, read directly, or 0 for null, a hole, or an index out of the list. */
    long element(long list, int index) {
        if (list == 0 || index < 0 || index >= API.readInt(list + layout.length)) return 0;
        long atomList = API.readLong(list + layout.atoms);
        if (atomList == 0) return 0;
        return API.readLong(atomList + layout.first + (long) index * Long.BYTES) & ByteUtils.ATOM_MASK;
    }

    /** Length of the list when last read, even if it couldn't be read. */
    int length() {
        return length;
    }

    /** Elements read, null ones included: see {@link #get}. */
    int count() {
        return count;
    }

    /** The object at the index of the elements read, or 0 for null (or a hole in an Array). */
    long get(int index) {
        return atoms.get(index) & ByteUtils.ATOM_MASK;
    }

    /** Frees the buffer, e.g. after a client reload. */
    void release() {
        atoms.release();
        length = count = 0;
    }
}
