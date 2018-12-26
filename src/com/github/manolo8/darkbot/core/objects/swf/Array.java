package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import static com.github.manolo8.darkbot.Main.API;

public class Array implements Updatable {

    private long address;
    public long[] elements;
    public int size;

    public Array(long address) {
        this.address = address;
        this.elements = new long[0];
    }

    @Override
    public void update() {
        int size = API.readMemoryInt(address + 56);

        if (size < 0 || size > 512) {
            return;
        } else {
            this.size = size;

            if (elements.length < size) {
                elements = new long[size];
            }
        }

        long table = API.readMemoryLong(address + 48) + 16;
        int length = size * 8;

        byte[] bytes = API.readMemory(table, length);

        int current = 0;

        for (int i = 0; i < length; i += 8) {
            long value = ByteUtils.getLong(bytes, i) - 1;
            //Can change at any time!
            if (value != -1 && current < elements.length) {
                elements[current++] = value;
            }
        }
    }

    @Override
    public void update(long address) {
        this.address = address;
    }
}
