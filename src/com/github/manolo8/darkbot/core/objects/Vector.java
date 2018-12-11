package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.def.Updatable;
import com.github.manolo8.darkbot.core.manager.BotManager;
import com.sun.jna.Memory;

import static com.github.manolo8.darkbot.Main.API;
import static java.lang.Math.min;

public class Vector implements Updatable {

    private long address;

    public Entry[] elements;
    public int size;

    public Vector(long address) {
        this.address = address;
        this.elements = new Entry[0];
    }

    @Override
    public void update() {

        long tableInfo = API.readMemoryLong(address + 32);
        long table = API.readMemoryLong(tableInfo + 8) - 2 /* why swf? */;
        size = API.readMemoryInt(tableInfo + 16);
        long length = (long) (Math.pow(2, API.readMemoryInt(tableInfo + 20)) * 4);

        if (elements.length < size) {
            if (size > 512 || size < 0) {
                //Informations of the array can change at any time, so, be carefull
                System.out.println("Array size is wrong: " + size + " ADDRESS: " + address);
                size = 0;
                return;
            } else {
                elements = new Entry[size];
            }
        }

        Memory memory = API.readMemory(table, length);

        length = min(memory.size(), length);

        int current = 0;

        int i;

        //Try to fix SWF issues why that???
        for (i = 0; i < length; i++) {
            if (memory.getInt(i) == BotManager.SEP) {
                i += 4;
                break;
            }
        }

        for (; i < length; i += 16) {
            long valueAddress = memory.getLong(i + 8);

            if (valueAddress >= 0 && valueAddress <= 10) continue;

            valueAddress--;

            if (current >= size) {
                break;
            } else if (elements[current] == null) {
                elements[current] = new Entry(API.readMemoryString(memory.getLong(i) - 2), valueAddress);
            } else if (elements[current].value != valueAddress) {
                elements[current].key = API.readMemoryString(memory.getLong(i) - 2);
                elements[current].value = valueAddress;
            }

            current++;
        }

        size = current - 1;
    }

    @Override
    public void update(long address) {
        this.address = address;
    }

    public Entry get(String key) {
        for (int i = 0; i < size; i++) {
            if (elements[i].key.equals(key)) {
                return elements[i];
            }
        }
        return null;
    }

    public class Entry {
        public String key;
        public long value;

        public Entry(String key, long value) {
            this.key = key;
            this.value = value;
        }
    }
}
