package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.swf.IntArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import static com.github.manolo8.darkbot.Main.API;

public class RepairManager implements Manager {
    boolean wasInDeadState;
    private long guiAddress, mainAddress, userDataAddress, repairAddress;

    private String killerName;
    private IntArray repairOptions = IntArray.ofArray(true);

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.guiManagerAddress.add(value -> {
            guiAddress = value;
            repairAddress = 0;
        });
        botInstaller.mainAddress.add(value -> mainAddress = value);
        botInstaller.heroInfoAddress.add(value -> userDataAddress = value);
    }

    public void tick() {
        if (!isDead()) {
            if (wasInDeadState) writeKiller();
            return;
        }
        wasInDeadState = true;
        if (repairAddress == 0) updateRepairAddr();

        killerName = API.readMemoryString(API.readMemoryLong(repairAddress + 0x68));
        repairOptions.update(API.readMemoryLong(repairAddress + 0x58));
    }

    private void updateRepairAddr() {
        long[] values = API.queryMemory(ByteUtils.getBytes(guiAddress, mainAddress), 1);
        if (values.length == 1) repairAddress = values[0] - 0x38;
    }

    private void writeKiller() {
        if (killerName == null || killerName.isEmpty()) System.out.println("You were destroyed by a radiation/mine/unknown");
        else System.out.println("You have been destroyed by: " + killerName);
        wasInDeadState = false;
    }

    public boolean isDead() {
        return API.readMemoryBoolean(userDataAddress + 0x4C);
    }
}
