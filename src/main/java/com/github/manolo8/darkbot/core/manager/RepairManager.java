package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.swf.IntArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.LogUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static com.github.manolo8.darkbot.Main.API;

public class RepairManager implements Manager {
    boolean wasInDeadState;
    private long guiAddress, mainAddress, userDataAddress, repairAddress;

    private String killerName;
    private IntArray repairOptions = IntArray.ofArray(true);

    private Map<String, OutputStream> streams = new HashMap<>();
    private String killerMessage;

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
            if (wasInDeadState) {
                writeKiller();
                if (!ConfigEntity.INSTANCE.getConfig().MISCELLANEOUS.LOG_DEATHS) return;

                writeToFile(LogUtils.START_TIME + "death");
            }
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
        killerMessage = killerName == null || killerName.isEmpty()
                ? "You were destroyed by a radiation/mine/unknown"
                : "You have been destroyed by: " + killerName;
        System.out.println(killerMessage);
        wasInDeadState = false;
    }

    public boolean isDead() {
        return API.readMemoryBoolean(userDataAddress + 0x4C);
    }

    private void writeToFile(String name) {
        try {
            OutputStream os = getOrCreateStream(name);
            if (os == null) return;

            os.write(formatLogMessage(killerMessage).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatLogMessage(String message) {
        return String.format("[%s] %-" + message.length() + "s" + System.lineSeparator(),
                LocalDateTime.now().format(LogUtils.LOG_DATE),
                message);
    }

    private OutputStream getOrCreateStream(String name) {
        return this.streams.computeIfAbsent(name, LogUtils::createLogFile);
    }

    public String getKillerName() { return killerName; }
}