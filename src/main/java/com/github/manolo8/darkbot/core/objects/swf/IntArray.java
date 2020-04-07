package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import static com.github.manolo8.darkbot.Main.API;

/**
 * Reads arrays with ints.
 * Instead of VectorInt & ArrayInt
 */
public class IntArray extends Updatable {
    private final int sizeOffset, tableOffset, bytesOffset;
    private final boolean isArray, autoUpdatable;

    public int[] elements = new int[0];
    public int size;

    public IntArray(int sizeOffset, int tableOffset, int bytesOffset, boolean isArray, boolean autoUpdatable) {
        this.sizeOffset = sizeOffset;
        this.tableOffset = tableOffset;
        this.bytesOffset = bytesOffset;
        this.isArray = isArray;
        this.autoUpdatable = autoUpdatable;
    }

    /**
     * Reads int Array {@code Array<int>}
     */
    public static IntArray ofArray() {
        return ofArray(false);
    }

    public static IntArray ofArray(boolean autoUpdatable) {
        return new IntArray(0x28, 0x20, 0x10, true, autoUpdatable);
    }

    /**
     * Reads int Vector {@code Vector<int>}
     */
    public static IntArray ofVector() {
        return ofVector(false);
    }

    public static IntArray ofVector(long address) {
        IntArray ofVec = ofVector(false);
        ofVec.update(address);
        return ofVec;
    }

    public static IntArray ofVector(boolean autoUpdatable) {
        return new IntArray(0x40, 0x30, 0x4, false, autoUpdatable);
    }

    public int getSize() {
        return Math.min(size, elements.length);
    }

    public int get(int idx) {
        return idx >= 0 && idx < getSize() ? elements[idx] : 0;
    }

    public int getLast() {
        return get(getSize() - 1);
    }

    @Override
    public void update() {
        size = API.readMemoryInt(address + sizeOffset);

        if (size < 0 || size > 1024) return;
        if (elements.length < size) elements = new int[(int) Math.min(size * 1.25, 1024)];

        long table = API.readMemoryLong(address + tableOffset) + bytesOffset;
        byte[] data = API.readMemory(table, size * getOffset());

        for (int i = 0, offset = 0; i < size; i++, offset += getOffset()) {
            elements[i] = ByteUtils.getInt(data, offset) >> (isArray ? 3 : 0);
        }
    }

    @Override
    public void update(long address) {
        super.update(address);
        if (autoUpdatable) update();
    }

    private int getOffset() {
        return isArray ? 8 : 4;
    }
}
