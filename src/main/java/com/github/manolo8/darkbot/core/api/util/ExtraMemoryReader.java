package com.github.manolo8.darkbot.core.api.util;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import java.util.function.Predicate;

public abstract class ExtraMemoryReader implements GameAPI.Memory {

    private final BotInstaller botInstaller;

    private byte[] tableData = new byte[0];
    private long lastTableUpdate;

    public ExtraMemoryReader(BotInstaller botInstaller) {
        this.botInstaller = botInstaller;
    }

    /**
     * @author Alph4rd
     */
    @Override
    public long searchClassClosure(Predicate<Long> pattern) {
        long mainAddress = botInstaller.mainApplicationAddress.get();
        if (mainAddress == 0) return 0;

        long time = System.currentTimeMillis();
        long table = Main.API.readMemoryLong(mainAddress, 0x10, 0x10, 0x18, 0x10, 0x28);
        int capacity = Main.API.readMemoryInt(table + 8) * 8;

        if (tableData.length < capacity) {
            tableData = new byte[capacity];
            Main.API.readMemory(table + 0x10, tableData, capacity);
            lastTableUpdate = System.currentTimeMillis() + 750; // cache for 750ms
        }

        if (lastTableUpdate < System.currentTimeMillis()) {
            Main.API.readMemory(table + 0x10, tableData, capacity);
            lastTableUpdate = System.currentTimeMillis() + 750; // cache for 750ms
        }

        for (int i = 0; i < capacity; i += 8) {
            long entry = ByteUtils.getLong(tableData, i);

            if (entry == 0) continue;

            long closure = Main.API.readMemoryLong(entry + 0x20);
            if (closure == 0 || closure == 0x200000001L) continue;

            if (pattern.test(closure)) return closure;
        }
        return 0;
    }
}
