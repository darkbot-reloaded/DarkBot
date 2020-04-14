package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.Lazy;

import static com.github.manolo8.darkbot.Main.API;

public class LogMediator extends Updatable {
    private ObjArray messageBuffer = ObjArray.ofArrStr();

    public long lastLogPtr = 0;
    public final Lazy<String> logs = new Lazy.NoCache<>(); // Can't cache the value, same log could appear twice

    public LogMediator() {
        logs.add(System.out::println);
    }

    public void update() {
        messageBuffer.update(API.readMemoryLong(address + 0x60));
        if (messageBuffer.size <= 0 || 50 < messageBuffer.size) return;

        for (int i = messageBuffer.indexOf(lastLogPtr) + 1; i < messageBuffer.getSize(); i++) {
            lastLogPtr = messageBuffer.get(i);
            String val = API.readMemoryString(API.readMemoryLong(lastLogPtr + 0x28));
            if (val == null || val.trim().isEmpty()) continue;
            logs.send(val);
        }
    }
}
