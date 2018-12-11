package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.def.Updatable;
import com.sun.jna.Memory;

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

        long table = API.readMemoryLong(address + 72) - 1;
        size = API.readMemoryInt(address + 80);
        long length = (long) (Math.pow(2, API.readMemoryInt(address + 84)) * 4);

        if (elements.length < size) {
            if (size > 512) {
                System.out.println("Array size is too big: " + size);
                return;
            } else {
                elements = new long[size];
            }
        }

        Memory memory = API.readMemory(table, length);

        int current = 0;

        for (int i = 32; i < length; i += 16) {
            long value = memory.getLong(i);
            //Can change any time!
            if (value != 0 && current < elements.length) {
                elements[current++] = memory.getLong(i) - 1;
            }
        }
    }

    @Override
    public void update(long address) {
        this.address = address;
    }
}
