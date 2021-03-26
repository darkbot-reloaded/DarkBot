package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.swf.IntArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.LogUtils;
import eu.darkbot.api.managers.RepairAPI;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static com.github.manolo8.darkbot.Main.API;

public class RepairManager implements Manager, RepairAPI {
    private boolean writtenToLog = true;
    private long guiAddress, mainAddress, userDataAddress, repairAddress;

    private String killerName;
    private final IntArray repairOptions = IntArray.ofArray(true);

    private final Map<String, OutputStream> streams = new HashMap<>();
    private final GuiManager guiManager;

    public RepairManager(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.guiManagerAddress.add(value -> {
            guiAddress = value;
            repairAddress = 0;
        });
        botInstaller.mainAddress.add(value -> mainAddress = value);
        botInstaller.heroInfoAddress.add(value -> userDataAddress = value);
    }

    private final List<Integer> repairOptionsList = new ArrayList<>();
    public void tick() {
        if (isDead()) writtenToLog = false;
        else {
            if (!writtenToLog) writeKiller();
            writtenToLog = true;
            return;
        }
        if (repairAddress == 0) updateRepairAddr();

        killerName = API.readMemoryString(API.readMemoryLong(repairAddress + 0x68));
        repairOptions.update(API.readMemoryLong(repairAddress + 0x58));

        repairOptionsList.clear();
        for (int i = 0; i < repairOptions.getSize(); i++)
            this.repairOptionsList.add(repairOptions.get(i));
    }

    private void updateRepairAddr() {
        long[] values = API.queryMemory(ByteUtils.getBytes(guiAddress, mainAddress), 1);
        if (values.length == 1) repairAddress = values[0] - 0x38;
    }

    public String getKillerName() {
        return killerName;
    }

    public boolean isDead() {
        if (userDataAddress != 0)
            return API.readMemoryBoolean(userDataAddress + 0x4C);
        else if (repairAddress != 0)
            return API.readMemoryBoolean(repairAddress + 40);
        else updateRepairAddr();
        return false;
    }

    public boolean canRespawn(int option) {
        for (int i = 0; i < repairOptions.getSize(); i++) {
            if (repairOptions.get(i) == option) return true;
        }
        return false;
    }

    public int[] getRespawnOptionsIds() {
        int[] options = new int[repairOptions.getSize()];
        for (int i = 0; i < repairOptions.getSize(); i++) {
            options[i] = repairOptions.get(i);
        }
        return options;
    }

    private int deaths;
    private Instant lastDeath;
    private void writeKiller() {
        String killerMessage = killerName == null || killerName.isEmpty()
                ? "You were destroyed by a radiation/mine/unknown"
                : "You have been destroyed by: " + killerName;
        System.out.println(killerMessage);
        deaths++;
        lastDeath = Instant.now();

        if (ConfigEntity.INSTANCE.getConfig().MISCELLANEOUS.LOG_DEATHS)
            writeToFile(LogUtils.START_TIME + "death", formatLogMessage(killerMessage));
    }

    private String formatLogMessage(String message) {
        return String.format("[%s] %-" + message.length() + "s" + System.lineSeparator(),
                LocalDateTime.now().format(LogUtils.LOG_DATE),
                message);
    }
    private void writeToFile(String name, String message) {
        try {
            OutputStream os = getOrCreateStream(name);
            if (os == null) return;

            os.write(message.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private OutputStream getOrCreateStream(String name) {
        return this.streams.computeIfAbsent(name, LogUtils::createLogFile);
    }

    @Override
    public int getDeathsAmount() {
        return deaths;
    }

    @Override
    public boolean isDestroyed() {
        return isDead();
    }

    @Override
    public void tryRevive(int repairOption) throws IllegalStateException {
        if (!isDead())
            throw new IllegalStateException("Ship already revived!");

        guiManager.tryRevive();
    }

    @Override
    public Collection<Integer> getAvailableRepairOptions() {
        return repairOptionsList;
    }

    @Override
    public @Nullable String getLastDestroyerName() {
        return getKillerName();
    }

    @Override
    public @Nullable Instant getLastDeathTime() {
        return lastDeath;
    }
}