package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.def.Updatable;
import com.sun.jna.Memory;

import static com.github.manolo8.darkbot.Main.API;
import static java.lang.Math.min;

public class ArrayDO implements Updatable {

    private long address;
    public long[] elements;
    public int size;

    public ArrayDO(long address) {
        this.address = address;
        this.elements = new long[0];
    }

    @Override
    public void update() {

        long table = API.readMemoryLong(address + 48);
        size = API.readMemoryInt(address + 56);
        long length = size * 8 + 16;

        if (elements.length < size) {
            if (size > 512) {
                //Informations of the array can change at any time, so, be carefull
                System.out.println("Array size is too big: " + size + " ADDRESS: " + address);
                size = 0;
                return;
            } else {
                elements = new long[size];
            }
        }

        Memory memory = API.readMemory(table, length);

        //check if is read
        length = min(memory.size(), length);

        int current = 0;

        for (int i = 16; i < length; i += 8) {
            long value = memory.getLong(i);
            //Can change at any time!
            if (value != 0 && current < elements.length) {
                elements[current++] = memory.getLong(i) - 1 /* why swf? */;
            }
        }
    }

    @Override
    public void update(long address) {
        this.address = address;
    }
}
