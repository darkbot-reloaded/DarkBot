package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.Lazy;

import static com.github.manolo8.darkbot.Main.API;

public class LogMediator extends Updatable {
    private ObjArray arrayObj = ObjArray.ofArrStr();

    public long lastLogPtr = 0;
    public final Lazy<String> logs = new Lazy.NoCache<>(); // Can't cache the value, same log could appear twice

    public LogMediator() {
        logs.add(System.out::println);
    }

    public void update() {
        arrayObj.update(API.readMemoryLong(address + 0x60));

        for (int i = arrayObj.indexOf(lastLogPtr) + 1; i < arrayObj.size; i++) {
            lastLogPtr = arrayObj.get(i);
            logs.send(API.readMemoryString(API.readMemoryLong(lastLogPtr + 0x28)));
        }
    }
}
