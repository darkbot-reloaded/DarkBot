package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.group.GroupMember;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.manolo8.darkbot.Main.API;

/**
 * Reads arrays in flash.
 * Instead of ArrayObj, VectorPtr & SpriteArray
 */
public class ObjArray extends Updatable implements SwfPtrCollection {
    private final int sizeOffset, tableOffset, bytesOffset;
    private final boolean isSprite, autoUpdatable;

    public int size;
    public long[] elements = new long[0];

    protected ObjArray(int sizeOffset, int tableOffset, int bytesOffset, boolean isSprite, boolean autoUpdatable) {
        this.sizeOffset    = sizeOffset;
        this.tableOffset   = tableOffset;
        this.bytesOffset   = bytesOffset;
        this.isSprite      = isSprite;
        this.autoUpdatable = autoUpdatable;
    }

    /**
     * Probably for {@code Array<String>}
     */
    public static ObjArray ofArrStr() {
        return ofArrStr(true);
    }

    public static ObjArray ofArrStr(boolean autoUpdatable) {
        return new ObjArray(0x28, 0x20, 0x10, false, autoUpdatable);
    }

    /**
     * Probably for {@code Array<Object>}
     */
    public static ObjArray ofArrObj() {
        return ofArrObj(true);
    }

    public static ObjArray ofArrObj(boolean autoUpdatable) {
        return new ObjArray(0x38, 0x20, 0x10, false, autoUpdatable);
    }

    /**
     * Reads children array of Sprite {@code Sprite}
     */
    public static ObjArray ofSprite() {
        return ofSprite(true);
    }

    public static ObjArray ofSprite(boolean autoUpdatable) {
        return new ObjArray(0x18, 0x8, 0x8, true, autoUpdatable);
    }

    /**
     * Reads {@code Vector<Object>}
     */
    public static ObjArray ofVector() {
        return ofVector(false);
    }

    public static ObjArray ofVector(boolean autoUpdatable) {
        return new ObjArray(0x38, 0x30, 0x10, false, autoUpdatable);
    }

    public int getSize() {
        return Math.min(size, elements.length);
    }

    public long getPtr(int idx) {
        return get(idx);
    }

    public long get(int idx) {
        return idx >= 0 && idx < getSize() ? elements[idx] : 0;
    }

    public long getLast() {
        return get(getSize() - 1);
    }

    public int indexOf(long value) {
        for (int i = getSize() - 1; i >= 0; i--) if (value == get(i)) return i;
        return -1;
    }

    @Override
    public void update() {
        size = API.readMemoryInt(address + sizeOffset);

        if (size < 0 || size > 8192 || address == 0) return;
        if (elements.length < size) elements = new long[Math.min((int) (size * 1.25), 8192)];

        long table = API.readMemoryLong(address + tableOffset) + bytesOffset;
        byte[] bytes = API.readMemory(table, size * 8);

        for (int i = 0, offset = 0; offset < bytes.length && i < size; offset += 8) {
            long value = ByteUtils.getLong(bytes, offset);
            if (value != 0) elements[i++] = value & ByteUtils.FIX;
        }
    }

    @Override
    public void update(long address) {
        super.update(isSprite ? API.readMemoryLong(address, 0x48, 0x40) : address);
        if (autoUpdatable) update();
    }
}
