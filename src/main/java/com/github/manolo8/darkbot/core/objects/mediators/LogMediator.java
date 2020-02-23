package com.github.manolo8.darkbot.core.objects.mediators;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.swf.ArrayObj;
import com.github.manolo8.darkbot.core.utils.Lazy;

import static com.github.manolo8.darkbot.Main.API;

public class LogMediator extends UpdatableAuto {
    private ArrayObj arrayObj = new ArrayObj();

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
