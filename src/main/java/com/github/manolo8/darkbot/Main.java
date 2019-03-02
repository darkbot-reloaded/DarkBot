package com.github.manolo8.darkbot;

import com.bulenkov.darcula.DarculaLaf;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.DarkBotAPI;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.*;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.modules.CollectorModule;
import com.github.manolo8.darkbot.modules.EventModule;
import com.github.manolo8.darkbot.modules.LootModule;
import com.github.manolo8.darkbot.modules.LootNCollectorModule;
import com.github.manolo8.darkbot.utils.ByteArrayToBase64TypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main extends Thread {
    public static final String VERSION = "1.13-beta15";

    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).create();

    public static final Object UPDATE_LOCKER = new Object();

    public static DarkBotAPI API;

    public final MapManager mapManager;
    public final StarManager starManager;
    public final HeroManager hero;
    public final GuiManager guiManager;
    public final StatsManager statsManager;
    public final PingManager pingManager;

    public final Lazy.Sync<Boolean> status;

    public Config config;
    public Module module;

    public long lastRefresh;

    private final BotInstaller botInstaller;
    private final MainGui form;

    public double avgTick;

    private volatile boolean running;

    public Main() {
        API = new DarkBotAPI();
        API.createWindow();

        this.config = new Config();

        loadConfig();
        if (config.MISCELLANEOUS.USE_DARCULA_THEME) {
            try {
                UIManager.setLookAndFeel(new DarculaLaf());
            } catch (UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
        }

        new ConfigEntity(config);

        botInstaller = new BotInstaller();
        status = new Lazy.Sync<>();

        guiManager = new GuiManager(this);
        starManager = new StarManager();
        mapManager = new MapManager(this);
        hero = new HeroManager(this);
        statsManager = new StatsManager(this);
        pingManager = new PingManager();

        botInstaller.add(guiManager);
        botInstaller.add(mapManager);
        botInstaller.add(hero);
        botInstaller.add(statsManager);
        botInstaller.add(pingManager);

        botInstaller.init();

        botInstaller.invalid.add(value -> {
            if (!value) lastRefresh = System.currentTimeMillis();
        });

        status.add(r -> lastRefresh = System.currentTimeMillis());

        updateConfig();

        form = new MainGui(this);

        start();
    }

    @Override
    public void run() {
        long time;

        while (true) {
            time = System.currentTimeMillis();

            tick();

            double tickTime = System.currentTimeMillis() - time;
            avgTick = ((avgTick * 9) + tickTime) / 10;
            sleepMax(time, botInstaller.invalid.value ? 10000 : 100);
        }
    }

    private void tick() {
        status.tick();

        if (isInvalid())
            invalidTick();
        else
            validTick();

        pingManager.tick();
        form.tick();

        checkConfig();
    }

    private boolean isInvalid() {
        return botInstaller.isInvalid();
    }

    private void invalidTick() {
        botInstaller.verify();
    }

    private void validTick() {
        guiManager.tick();
        guiManager.pet.tickActive();
        hero.tick();
        mapManager.tick();
        statsManager.tick();

        if (running && guiManager.canTickModule())
            tickRunning();
    }

    private void tickRunning() {
        guiManager.pet.tick();
        module.tick();
        checkPetBug();
        checkRefresh();
    }

    private void checkConfig() {
        if (config.changed) {
            config.changed = false;
            saveConfig();
        }
    }

    private void checkPetBug() {
        if (mapManager.isTarget(hero.pet)) hero.pet.clickable.setRadius(0);
    }

    private void checkRefresh() {
        if (config.MISCELLANEOUS.REFRESH_TIME != 0) {

            boolean refreshTimer = System.currentTimeMillis() - lastRefresh > config.MISCELLANEOUS.REFRESH_TIME * 60 * 1000;
            boolean canRefresh = module.canRefresh();

            if (refreshTimer && canRefresh) {
                API.refresh();
                lastRefresh = System.currentTimeMillis();
            }
        }
    }

    public <A extends Module> A setModule(A module) {
        module.install(this);
        this.module = module;
        return module;
    }

    public void setRunning(boolean running) {
        if (this.running != running) {

            status.send(running);

            this.running = running;
        }
    }

    private void loadConfig() {
        try {

            File config = new File("config.json");

            if (config.exists()) {

                FileReader reader = new FileReader(config);

                this.config = GSON.fromJson(reader, Config.class);

                if (this.config == null) this.config = new Config();

                reader.close();

            } else {
                saveConfig();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        try {

            File config = new File("config.json");

            FileWriter writer = new FileWriter(config);

            GSON.toJson(this.config, writer);

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return running;
    }

    private void updateConfig() {
        switch (config.CURRENT_MODULE) {
            case 0:
                if (isNotModule(CollectorModule.class)) {
                    setModule(new CollectorModule());
                }
                break;
            case 1:
                if (isNotModule(LootModule.class)) {
                    setModule(new LootModule());
                }
                break;
            case 2:
                if (isNotModule(LootNCollectorModule.class)) {
                    setModule(new LootNCollectorModule());
                }
                break;
            case 3:
                if (isNotModule(EventModule.class)) {
                    setModule(new EventModule());
                }
                break;
            default:
                setModule(new CollectorModule());
        }
    }

    private boolean isNotModule(Class clazz) {
        return module == null || module.getClass() != clazz;
    }

    private void sleepMax(long time, int total) {
        time = System.currentTimeMillis() - time;

        if (time < total) {
            try {
                Thread.sleep(total - time);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
