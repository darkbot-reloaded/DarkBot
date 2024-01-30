package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.NativeUpdatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.Offsets;
import it.unimi.dsi.fastutil.longs.AbstractLongList;

import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.LongConsumer;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.core.objects.swf.FlashMap.DICTIONARY_FLAG;

public abstract class FlashListLong extends AbstractLongList implements NativeUpdatable, RandomAccess {

    protected long[] elements = new long[0];

    private long address;
    private long lastPointer;
    private int size;

    private boolean autoUpdate = true;

    public static FlashListLong ofArray() {
        return new FlashArray();
    }

    public static FlashListLong ofVector() {
        return new FlashVector();
    }

    public static FlashListLong ofSprite() {
        return new SpriteList();
    }

    public static FlashListLong ofMapKeys() {
        return new FlashMapValues(true);
    }

    public static FlashListLong ofMapValues() {
        return new FlashMapValues(false);
    }

    public FlashListLong noAuto() {
        this.autoUpdate = false;
        return this;
    }

    @Override
    public long getAddress() {
        return address;
    }

    @Override
    public void update(long address) {
        if (this.address != address) setSize(0);
        this.address = address;
        if (autoUpdate) update();
    }

    @Override
    public abstract void update();

    @Override
    public long getLong(int index) {
        return elements[Objects.checkIndex(index, size())];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int indexOf(long k) {
        for (int i = 0; i < size(); i++) {
            if (k == getLong(i)) return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(long k) {
        for (int i = size() - 1; i >= 0; i--)
            if (k == getLong(i)) return i;
        return -1;
    }

    public void forEachIncremental(LongConsumer consumer) {
        for (int i = lastIndexOf(lastPointer) + 1; i < size(); i++)
            consumer.accept(lastPointer = getLong(i));
    }

    public long getOrDefault(int index, long fallback) {
        if (index < 0 || index >= size()) return fallback;
        return getLong(index);
    }

    /**
     * @return last element or 0 if is empty
     */
    public long getLastElement() {
        return getOrDefault(size() - 1, 0);
    }

    protected void setSize(int size) {
        this.size = size;
    }

    protected boolean insufficientCapacity(int size, int maxSize) {
        if (size <= 0 || size > maxSize) return true;
        // don't need a copy, just create new
        if (size > elements.length)
            elements = new long[Math.min((int) (size * 1.25), maxSize)];
        return false;
    }

    protected void update(int sizeOffset, int tableOffset, int tableSkip, boolean skipHoles) {
        if (getAddress() == 0) return;

        int size = readInt(sizeOffset);
        if (insufficientCapacity(size, FlashList.MAX_SIZE)) {
            setSize(0);
        } else {
            int maxLength = size * 8;
            long table = readLong(tableOffset) + tableSkip;

            int realSize = 0;
            long[] elements = this.elements;
            for (int offset = 0; offset < maxLength; offset += 8) {
                long atom = Main.API.readLong(table, offset);
                // hole in the array -> skip
                if (skipHoles && atom == 0) continue;
                elements[realSize++] = atom & ByteUtils.ATOM_MASK;
            }
            setSize(realSize);
        }
    }

    private static class FlashArray extends FlashListLong {
        @Override
        public void update() {
            update(40, 32, 16, true);
        }
    }

    private static class FlashVector extends FlashListLong {
        @Override
        public void update() {
            update(56, 48, 16, false);
        }
    }

    private static class SpriteList extends FlashListLong {
        @Override
        public void update() {
            int osOffset = Offsets.SPRITE_OFFSET;
            update(24 + osOffset, 8 + osOffset, 8, false);
        }

        @Override
        public void update(long address) {
            super.update(Main.API.readLong(address, 72, 64));
        }
    }

    public static class FlashMapValues extends FlashListLong {
        private final boolean ofKeys;

        public FlashMapValues(boolean ofKeys) {
            this.ofKeys = ofKeys;
        }

        @Override
        public void update() {
            if (getAddress() == 0) return;
            long traits = readLong(16, 40);

            int tableOffset = API.readInt(traits, 236);
            if (tableOffset == 0) return;

            boolean isDictionary = (API.readInt(traits, 248) & DICTIONARY_FLAG) != 0;
            if (isDictionary) {
                readHashTable(readLong(tableOffset) + 8); //read hash table ptr & skip cpp vtable
            } else {
                readHashTable(getAddress() + tableOffset);
            }
        }

        private int getCapacity(int logCapacity) {
            if (logCapacity <= 0) return 0;
            return (1 << (logCapacity - 1)) * Long.BYTES;
        }

        private void readHashTable(long table) {
            int size = API.readInt(table, 8); // includes deleted items
            int capacity = getCapacity(API.readInt(table, 12));
            if (capacity <= 0 || capacity > FlashMap.MAX_CAPACITY || insufficientCapacity(size, FlashMap.MAX_SIZE)) {
                setSize(0);
            } else {
                long atomsAndFlags = API.readLong(table);
                long atoms = (atomsAndFlags & ByteUtils.ATOM_MASK) + 8;

                int currentSize = 0, realSize = 0;
                for (int offset = 0; offset < capacity && currentSize < size; offset += 8) {
                    long keyAtom = API.readLong(atoms, offset);

                    if (keyAtom == FlashMap.EMPTY_ITEM) continue;
                    offset += 8; // value is always after the key even if is deleted

                    if (keyAtom == FlashMap.DELETED_ITEM) {
                        currentSize++;
                        continue; // skip deleted pair
                    }
                    if (ofKeys) {
                        elements[realSize] = keyAtom & ByteUtils.ATOM_MASK;
                    } else {
                        long valueAtom = API.readLong(atoms, offset);
                        elements[realSize] = valueAtom & ByteUtils.ATOM_MASK;
                    }
                    realSize++;
                    currentSize++;
                }
                setSize(realSize);
            }
        }
    }
}
