package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import static com.github.manolo8.darkbot.Main.API;

public class VectorPtr extends Updatable {
    public long[] elements;
    public int size;

    private final int sizeOffset, tableOffset, bytesOffset;

    public VectorPtr() {
        this(0);
    }

    public VectorPtr(long address) {
        this(address, 56, 48, 16);
    }

    private VectorPtr(int sizeOffset, int tableOffset, int bytesOffset) {
        this(0, sizeOffset, tableOffset, bytesOffset);
    }

    public VectorPtr(long address, int sizeOffset, int tableOffset, int bytesOffset) {
        this.address = address;
        this.sizeOffset = sizeOffset;
        this.tableOffset = tableOffset;
        this.bytesOffset = bytesOffset;
        this.elements = new long[0];
    }

    public static VectorPtr ofEntity() {
        return new VectorPtr();
    }

    public static VectorPtr ofPet() {
        return new VectorPtr(24, 8, 8);
    }

    public static VectorPtr ofPetCheck() {
        return new VectorPtr(56, 32, 16);
    }

    public long get(int idx) {
        return idx >= 0 && idx < size ? elements[idx] : 0;
    }

    @Override
    public void update() {
        size = API.readMemoryInt(address + sizeOffset);

        if (size < 0 || size > 8192 || address == 0) return;
        if (elements.length < size) elements = new long[Math.min((int) (size * 1.25), 8192)];

        long table = API.readMemoryLong(address + tableOffset) + bytesOffset;
        int length = size * 8;
        byte[] bytes = API.readMemory(table, length);

        for (int current = 0, i = 0; current < size && i < length; i += 8) {
            long value = ByteUtils.getLong(bytes, i);
            if (value != 0) elements[current++] = value & 0xfffffffffff8L;
        }
    }
}
