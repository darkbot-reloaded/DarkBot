package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.core.utils.Lazy;

import java.util.HashMap;
import java.util.function.Consumer;

import static com.github.manolo8.darkbot.Main.API;

public class Dictionary implements Updatable {

    private long address;

    private HashMap<String, Lazy<Long>> fast;

    public Entry[] elements;
    public int size;

    private int lastFix;

    public Dictionary(long address) {
        this.address = address;
        this.elements = new Entry[0];
    }

    public void addLazy(String key, Consumer<Long> consumer) {
        if (fast == null) fast = new HashMap<>();

        Lazy<Long> lazy = fast.get(key);

        if (lazy == null) {
            lazy = new Lazy<>();
            fast.put(key, lazy);
        }

        lazy.add(consumer);
    }

    @Override
    public void update() {

        long tableInfo = API.readMemoryLong(address + 32);
        long table = API.readMemoryLong(tableInfo + 8) - 4;
        int size = API.readMemoryInt(tableInfo + 16);
        int length = (int) (Math.pow(2, API.readMemoryInt(tableInfo + 20)) * 4) + lastFix + 4;

        if (length > 4096 || length < 0) return;

        if (elements.length < size) {
            if (size > 128 || size < 0) {
                //Informations of the array can change at any time, so, be carefull
                System.out.println("Array size is wrong: " + size + " ADDRESS: " + address);
                this.size = 0;
                return;
            } else {
                elements = new Entry[size];
            }
        }

        byte[] bytes = API.readMemory(table, length);

        int i;

//        Try to fix SWF issues why that???
        if (lastFix >= 0 && bytes.length > lastFix && ByteUtils.getInt(bytes, lastFix) == BotInstaller.SEP) {
            i = lastFix + 4;
        } else {
            for (i = 0; i < length; i++) {
                if (ByteUtils.getInt(bytes, lastFix) == BotInstaller.SEP) {
                    lastFix = i;
                    //return to redo
                    return;
                }
            }
        }

        int current = 0;

        for (; i < length; i += 16) {
            long valueAddress = ByteUtils.getLong(bytes, i + 8);

            if (valueAddress >= 0 && valueAddress <= 10) continue;

            valueAddress--;

            if (current >= size) {
                break;
            } else if (elements[current] == null) {
                elements[current] = new Entry(API.readMemoryString(ByteUtils.getLong(bytes, i) - 2), valueAddress);

                //send update
                send(elements[current].key, valueAddress);

            } else if (elements[current].value != valueAddress) {

                //send reset only if not updated yet
                sendReset(elements[current].key, current - 1);

                elements[current].key = API.readMemoryString(ByteUtils.getLong(bytes, i) - 2);
                elements[current].value = valueAddress;

                //send update
                send(elements[current].key, valueAddress);
            }

            current++;
        }

        size = current - 1;

        if (this.size > size) {
            while (size < this.size && this.size > 0) {
                //send reset only if not updated yet two
                sendReset(elements[--this.size].key, size);
            }
        } else {
            this.size = size;
        }
    }

    private void send(String str, long value) {
        if (fast != null) {
            Lazy<Long> lazy = fast.get(str);
            if (lazy != null) lazy.send(value);
        }
    }

    private void sendReset(String str, int current) {
        for (int i = 0; i < current; i++) {
            if (elements[i].key.equals(str)) {
                return;
            }
        }

        send(str, 0);
    }

    @Override
    public void update(long address) {
        this.address = address;
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
