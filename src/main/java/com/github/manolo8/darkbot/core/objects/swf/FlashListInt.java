package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.NativeUpdatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import it.unimi.dsi.fastutil.ints.AbstractIntList;

import java.util.Objects;
import java.util.RandomAccess;

public abstract class FlashListInt extends AbstractIntList implements NativeUpdatable, RandomAccess {
    protected int[] elements = new int[0];

    private int size;
    private long address;

    private boolean autoUpdate = true;

    public static FlashListInt ofArray() {
        return new IntArray();
    }

    public static FlashListInt ofVector() {
        return new IntVector();
    }

    public FlashListInt noAuto() {
        this.autoUpdate = false;
        return this;
    }

    /**
     * does not throw {@link IndexOutOfBoundsException}, returns 0 instead
     * to check if value is correct check size first
     */
    @Override
    public int getInt(int index) {
        return elements[Objects.checkIndex(index, size())];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int indexOf(int k) {
        for (int i = 0; i < size(); i++) {
            if (k == getInt(i)) return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(int k) {
        for (int i = size() - 1; i >= 0; i--)
            if (k == getInt(i)) return i;
        return -1;
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

    public int getOrDefault(int index, int fallback) {
        if (index < 0 || index >= size()) return fallback;
        return getInt(index);
    }

    /**
     * @return last element or 0 if is empty
     */
    public int getLastElement() {
        return getOrDefault(size() - 1, 0);
    }

    protected void setSize(int size) {
        this.size = size;
    }

    private boolean insufficientCapacity(int size) {
        if (size <= 0 || size > FlashList.MAX_SIZE) return true;
        // don't need a copy, just create new
        if (size > elements.length)
            elements = new int[Math.min((int) (size * 1.25), FlashList.MAX_SIZE)];
        return false;
    }

    public abstract void update();

    protected abstract void readElements(int size);

    protected void update(int sizeOffset) {
        if (getAddress() == 0) return;

        int size = readInt(sizeOffset);
        if (insufficientCapacity(size)) {
            setSize(0);
        } else {
            readElements(size);
        }
    }

    private static class IntArray extends FlashListInt {

        @Override
        public void update() {
            update(40);
        }

        @Override
        protected void readElements(int size) {
            int maxLength = size * 8;
            long table = readLong(32) + 16;

            int realSize = 0;
            int[] elements = this.elements;
            for (int offset = 0; offset < maxLength; offset += 8) {
                long atom = Main.API.readLong(table, offset);
                if (atom == 0) continue;
                elements[realSize++] = (int) ((atom & ByteUtils.ATOM_MASK) >> 3);
            }
            setSize(realSize);
        }
    }

    private static class IntVector extends FlashListInt {

        @Override
        public void update() {
            super.update(64);
        }

        @Override
        protected void readElements(int size) {
            int maxLength = size * 4;
            long table = readLong(48) + 4;

            int realSize = 0;
            int[] elements = this.elements;
            for (int offset = 0; offset < maxLength; offset += 4) {
                elements[realSize++] = Main.API.readInt(table, offset);
            }
            setSize(realSize);
        }
    }
}
