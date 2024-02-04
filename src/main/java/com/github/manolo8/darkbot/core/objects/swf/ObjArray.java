package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.Offsets;

import static com.github.manolo8.darkbot.Main.API;

/**
 * Reads arrays in flash.
 * Instead of ArrayObj, VectorPtr & SpriteArray
 * Used for SpriteArray
 */
@Deprecated
public class ObjArray extends SwfPtrCollection {
    private static final int MAX_SIZE = 8192;
    private static final byte[] BUFFER = new byte[8192 * 8]; //64kb

    private final int sizeOffset, tableOffset, bytesOffset;
    private final boolean autoUpdatable;

    public int size;
    private long[] elements = new long[0];

    protected ObjArray(int sizeOffset, int tableOffset, int bytesOffset, boolean autoUpdatable) {
        this.sizeOffset    = sizeOffset;
        this.tableOffset   = tableOffset;
        this.bytesOffset   = bytesOffset;
        this.autoUpdatable = autoUpdatable;
    }

    /**
     * Probably for {@code Array<String>}
     */
    public static ObjArray ofArrStr() {
        return ofArrStr(true);
    }

    public static ObjArray ofArrStr(boolean autoUpdatable) {
        return new ObjArray(0x28, 0x20, 0x10, autoUpdatable);
    }

    /**
     * Probably for {@code Array<Object>}
     */
    public static ObjArray ofArrObj() {
        return ofArrObj(true);
    }

    public static ObjArray ofArrObj(boolean autoUpdatable) {
        return new ObjArray(0x38, 0x20, 0x10, autoUpdatable);
    }

    /**
     * Reads children array of Sprite {@code Sprite}
     */
    public static ObjArray ofSprite() {
        return ofSprite(true);
    }

    public static ObjArray ofSprite(boolean autoUpdatable) {
        return new ObjArray(0x18 + Offsets.SPRITE_OFFSET, 0x8 + Offsets.SPRITE_OFFSET, 0x8, autoUpdatable);
    }

    /**
     * Reads {@code Vector<Object>}
     */
    public static ObjArray ofVector() {
        return ofVector(false);
    }

    public static ObjArray ofVector(boolean autoUpdatable) {
        return new ObjArray(0x38, 0x30, 0x10, autoUpdatable);
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

    @Override
    public void update() {
        size = API.readInt(address + sizeOffset);

        if (size < 0 || size > MAX_SIZE || address == 0) return;
        if (elements.length < size) elements = new long[Math.min((int) (size * 1.25), MAX_SIZE)];

        long table = API.readLong(address + tableOffset) + bytesOffset;
        int length = size * 8;
        API.readBytes(table, BUFFER, length);

        for (int i = 0, offset = 0; offset < length && i < size; offset += 8) {
            long value = ByteUtils.getLong(BUFFER, offset);
            elements[i++] = value & ByteUtils.ATOM_MASK; //not sure if we should skip 0 values
        }
    }

    @Override
    public void update(long address) {
        super.update(isSprite() ? API.readLong(address, 0x48, 0x40) : address);
        if (autoUpdatable) update();
    }

    private boolean isSprite() {
        return this.sizeOffset == (0x18 + Offsets.SPRITE_OFFSET);
    }
}
