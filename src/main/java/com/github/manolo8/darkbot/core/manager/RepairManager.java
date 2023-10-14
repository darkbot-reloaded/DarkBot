package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.entities.bases.BaseRepairStation;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.SpriteObject;
import com.github.manolo8.darkbot.core.objects.swf.IntArray;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.extensions.features.handlers.ReviveSelectorHandler;
import com.github.manolo8.darkbot.utils.LogUtils;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.extensions.selectors.PrioritizedSupplier;
import eu.darkbot.api.extensions.selectors.ReviveSelector;
import eu.darkbot.api.game.enums.ReviveLocation;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.RepairAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.manolo8.darkbot.Main.API;

public class RepairManager implements Manager, RepairAPI {
    private final Main main;
    private final ReviveSelectorHandler reviveHandler;

    private final Map<String, OutputStream> streams = new HashMap<>();
    private final List<Integer> repairOptionsList = new ArrayList<>();

    private final IntArray repairOptions = IntArray.ofArray(true);
    private final ObjArray repairTypes = ObjArray.ofVector(true);

    private final byte[] patternCache = new byte[40];

    private String killerName;
    private Locatable deathLocation;
    private Instant lastDeath;

    private boolean destroyed, shouldInstantRepair = false;
    private long userDataAddress, repairAddress, beforeReviveTime, afterAvailableWait, lastReviveAttempt;
    private int deaths;

    public RepairManager(Main main, ReviveSelectorHandler reviveHandler) {
        this.main = main;
        this.reviveHandler = reviveHandler;
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.guiManagerAddress.add(value -> repairAddress = 0);
        botInstaller.heroInfoAddress.add(value -> userDataAddress = value);
    }

    public String getStatus() {
        ReviveLocation location = reviveHandler.getBest();
        int availableIn = optionAvailableIn(getRepairOptionFromType(location));

        int beforeRevive = (int) (((beforeReviveTime + (main.config.GENERAL.SAFETY.WAIT_BEFORE_REVIVE * 1000L))
                - System.currentTimeMillis()) / 1000);

        return "Reviving at: " + location + ", in " + Math.max(beforeRevive, availableIn) + "s";
    }

    public boolean setBeforeReviveTime() {
        if (beforeReviveTime == -1)
            beforeReviveTime = System.currentTimeMillis();

        return System.currentTimeMillis() - beforeReviveTime < (main.config.GENERAL.SAFETY.WAIT_BEFORE_REVIVE * 1000L);
    }

    private void checkInstantRepair() {
        if (!shouldInstantRepair || !main.isRunning()
                || main.hero.getHealth().getMaxHp() == 0 || main.config.GENERAL.SAFETY.INSTANT_REPAIR == 0) return;
        // have ~25% hp already after revive - do not use instant repair. maybe create setting for min health
        if (main.hero.getHealth().hpPercent() >= 0.25) {
            shouldInstantRepair = false;
            return;
        }

        if (lastReviveAttempt + 15_000 > System.currentTimeMillis()) {
            main.mapManager.entities.basePoints.stream()
                    .filter(basePoint -> basePoint instanceof BaseRepairStation)
                    .findAny()
                    .filter(basePoint -> basePoint.clickable.enabled)
                    .ifPresent(basePoint -> {
                        BaseRepairStation repairStation = (BaseRepairStation) basePoint;

                        int currentRepairs = repairStation.getInstantRepairs();
                        if (currentRepairs >= main.config.GENERAL.SAFETY.INSTANT_REPAIR) {
                            repairStation.clickable.click();
                            System.out.println("Used instant repair! " + currentRepairs);
                        }

                        shouldInstantRepair = false;
                    });
        }
    }

    public void tick() {
        boolean alive = isAlive();

        if (alive) {
            if (main.hero.address != 0) // possibly alive but we are not sure yet
                destroyed = false;

            checkInstantRepair();
            beforeReviveTime = -1;
            return;
        }

        if (!destroyed) {
            shouldInstantRepair = true;
            destroyed = true;
            deaths++;
            lastDeath = Instant.now();
            if (userDataAddress != 0) // only if hero was alive
                deathLocation = main.hero.getLocationInfo().getCurrent().copy();

            killerName = API.readMemoryStringFallback(API.readMemoryLong(repairAddress + 0x68), null);

            String killerMessage = killerName == null || killerName.isEmpty()
                    ? "You were destroyed by a radiation/mine/unknown"
                    : "You have been destroyed by: " + killerName;
            System.out.println(killerMessage);

            if (ConfigEntity.INSTANCE.getConfig().MISCELLANEOUS.LOG_DEATHS)
                writeToFile("deaths_" + LogUtils.START_TIME, formatLogMessage(killerMessage));
        }

        repairOptions.update(API.readMemoryLong(repairAddress + 0x58));

        repairOptionsList.clear();
        for (int i = 0; i < repairOptions.getSize(); i++)
            this.repairOptionsList.add(repairOptions.get(i));

        repairTypes.update(API.readMemoryLong(repairAddress + 0x60));
    }

    // return true if clicked, false if should wait
    public boolean tryRevive() {
        // game did cleanup in Repair Manager, nothing to do here anymore. if nothing happens then only reload left
        if (repairOptions.getSize() <= 0) return false;

        int repairOption = getRepairOptionFromType(reviveHandler.getBest());
        int availableIn = optionAvailableIn(repairOption);

        if (availableIn > 0) {
            afterAvailableWait = System.currentTimeMillis();
            return false;
        }

        // wait 1 second after the option is available to potentially fix in-game bug
        if (System.currentTimeMillis() - afterAvailableWait < 1000) return false;

        if (repairOption != -1)
            API.writeMemoryLong(repairAddress + 32, repairOption);

        int selected = API.readInt(repairAddress + 32);
        // if any of these is selected, call this method with null param may result in crash
        if (selected == 0 || selected == 9
                || !API.callMethodChecked(false, "23(2626)1016341800", 5, repairAddress, 0L)) {
            SpriteObject repairGui = new SpriteObject();
            repairGui.update(Main.API.readLong(repairAddress + 48));
            repairGui.update();
            API.mouseClick(repairGui.x() + 263, repairGui.y() + 426);
        }
        lastReviveAttempt = System.currentTimeMillis();

        return true;
    }

    private boolean isAlive() {
        if (repairAddress != 0) return !API.readMemoryBoolean(repairAddress + 0x28);

        if (main.mapManager.mapAddress == 0 || main.guiManager.lostConnection.isVisible()
                || main.guiManager.connecting.isVisible() || (main.hero.address != 0 && main.hero.id != 0))
            return true;

        if (userDataAddress == 0 || API.readMemoryBoolean(userDataAddress + 0x4C)) {
            long repairClosure = API.searchClassClosure(this::repairClosurePattern);
            if (repairClosure == 0) return true;

            repairAddress = API.readLong(repairClosure + 72); // check on next tick
        }

        return true;
    }

    @Deprecated
    public String getKillerName() {
        return killerName;
    }

    @Deprecated
    public boolean isDead() {
        return isDestroyed();
    }

    @Deprecated
    public boolean canRespawn(int option) {
        return repairOptionsList.contains(option);
    }

    public void resetDeaths() {
        deaths = 0;
    }

    private int optionAvailableIn(int repairOption) {
        if (repairOption == -1) return -1;
        return API.readInt(repairTypes.get(repairOption) + 48);
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

    private boolean repairClosurePattern(long addr) {
        API.readMemory(addr + 48, patternCache);

        return ByteUtils.getInt(patternCache, 0) == 0
                && ByteUtils.getInt(patternCache, 4) == 1
                && ByteUtils.getInt(patternCache, 8) == 2
                && ByteUtils.getInt(patternCache, 12) == 3
                && ByteUtils.getInt(patternCache, 16) == 4
                && ByteUtils.getInt(patternCache, 20) == 0 // align to 8
                && API.readLong(ByteUtils.getLong(patternCache, 24), 0x10) != 0
                && API.readLong(ByteUtils.getLong(patternCache, 32), 0x10) != 0;
    }

    private int getRepairOptionFromType(ReviveLocation reviveLocation) {
        for (int i = 0; i < repairOptions.getSize(); i++) {
            if (ReviveLocation.of(repairOptions.get(i)) == reviveLocation) {
                return repairOptions.get(i);
            }
        }

        return -1;
    }

    @Override
    public int getDeathAmount() {
        return deaths;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public int isAvailableIn(ReviveLocation reviveLocation) {
        return optionAvailableIn(getRepairOptionFromType(reviveLocation));
    }

    @Override
    public @Nullable String getLastDestroyerName() {
        return getKillerName();
    }

    @Override
    public @Nullable Instant getLastDeathTime() {
        return lastDeath;
    }

    @Override
    public @Nullable Locatable getLastDeathLocation() {
        return deathLocation;
    }

    @Feature(name = "Revive Supplier", description = "Provides a place where ship should be revived")
    public static class DefaultReviveSupplier implements ReviveSelector, PrioritizedSupplier<ReviveLocation> {

        private final ConfigSetting<com.github.manolo8.darkbot.config.types.suppliers.ReviveLocation> reviveLocation;

        public DefaultReviveSupplier(ConfigAPI config) {
            this.reviveLocation = config.requireConfig("general.safety.revive");
        }

        @Override
        public @NotNull PrioritizedSupplier<ReviveLocation> getReviveLocationSupplier() {
            return this;
        }

        @Override
        public ReviveLocation get() {
            return ReviveLocation.values()[reviveLocation.getValue().ordinal() + 1];
        }
    }
}