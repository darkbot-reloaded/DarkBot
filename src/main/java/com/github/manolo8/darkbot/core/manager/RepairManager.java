package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.swf.IntArray;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.LogUtils;
import eu.darkbot.api.managers.RepairAPI;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.manolo8.darkbot.Main.API;

public class RepairManager implements Manager, RepairAPI {
    private final Main main;
    private boolean writtenToLog = true;
    private long userDataAddress, repairAddress;

    private String killerName;
    private final IntArray repairOptions = IntArray.ofArray(true);
    private final ObjArray repairTypes = ObjArray.ofVector(true);

    private final Map<String, OutputStream> streams = new HashMap<>();
    private final GuiManager guiManager;

    public RepairManager(Main main, GuiManager guiManager) {
        this.main = main;
        this.guiManager = guiManager;
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.guiManagerAddress.add(value -> {
            repairAddress = 0;
        });
        botInstaller.heroInfoAddress.add(value -> userDataAddress = value);
    }

    private final List<Integer> repairOptionsList = new ArrayList<>();
    public void tick() {
        if (!isDead()) {
            if (!writtenToLog) {
                writeKiller();
                writtenToLog = true;
            }

            return;
        }
        if (repairAddress == 0) updateRepairAddr();
        if (repairAddress == 0) return;

        killerName = API.readMemoryString(API.readMemoryLong(repairAddress + 0x68));
        repairOptions.update(API.readMemoryLong(repairAddress + 0x58));

        repairOptionsList.clear();
        for (int i = 0; i < repairOptions.getSize(); i++)
            this.repairOptionsList.add(repairOptions.get(i));

        repairTypes.update(API.readMemoryLong(repairAddress + 0x60));
    }

    private void updateRepairAddr() {
        long repairClosure = API.searchClassClosure(this::repairClosurePattern);
        if (repairClosure == 0) return;

        repairAddress = API.readLong(repairClosure + 72);
    }

    public String getKillerName() {
        return killerName;
    }

    public boolean isDead() {
        if (repairAddress != 0) {
            boolean isReallyDead = API.readMemoryBoolean(repairAddress + 0x28);
            if (isReallyDead) writtenToLog = false;

            return isReallyDead; // the below ones except userData are only assumptions
        }

        // user data is initialized only if hero was initialized before death
        if (userDataAddress != 0) return API.readMemoryBoolean(userDataAddress + 0x4C);

        return main.mapManager.mapAddress != 0
               && !guiManager.lostConnection.isVisible()
               && !guiManager.connecting.isVisible()
               && (main.hero.address == 0 || main.hero.id == 0); // still does a query on startup, same like in GuiManager
    }

    public boolean canRespawn(int option) {
        return repairOptionsList.contains(option);
    }

    public int[] getRespawnOptionsIds() {
        int[] options = new int[repairOptions.getSize()];
        for (int i = 0; i < repairOptions.getSize(); i++) {
            options[i] = repairOptions.get(i);
        }
        return options;
    }

    private int getCooldown(int repairOption) {
        return API.readInt(repairTypes.get(repairOption) + 48);
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

    private final byte[] cache = new byte[40];
    private boolean repairClosurePattern(long addr){
        API.readMemory(addr + 48, cache);

        return ByteUtils.getInt(cache, 0) == 0
               && ByteUtils.getInt(cache, 4) == 1
               && ByteUtils.getInt(cache, 8) == 2
               && ByteUtils.getInt(cache, 12) == 3
               && ByteUtils.getInt(cache, 16) == 4
               && ByteUtils.getInt(cache, 20) == 0 // align to 8
               && API.readLong(ByteUtils.getLong(cache, 24), 0x10) != 0
               && API.readLong(ByteUtils.getLong(cache, 32), 0x10) != 0;
    }

    @Override
    public int getDeathAmount() {
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