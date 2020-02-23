package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;

import java.util.ArrayList;

import static com.github.manolo8.darkbot.Main.API;

public class ArrayObj extends UpdatableAuto {
    public int size;
    public long[] elements;

    public ArrayObj() {
        this.elements = new long[0];
    }

    public long get(int idx) {
        return idx >= 0 && idx < size ? elements[idx] : 0;
    }

    public int indexOf(long value) {
        for (int i = size - 1; i >= 0; i--) if (value == elements[i]) return i;
        return -1;
    }

    @Override
    public void update() {
        size = API.readMemoryInt(address + 0x28);

        if (size < 0 || size > 2048) return;
        if (elements.length < size) elements = new long[Math.min((int) (size * 1.25), 2048)];

        long table = API.readMemoryLong(address + 0x20) + 16;
        for (int offset = 0, i = 0; offset < 8096 && i < size; offset += 8) {
            long addr = API.readMemoryLong(table + offset) & EntryArray.FIX;
            if (addr != 0) elements[i++] = addr;
        }
    }
}
