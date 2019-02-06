package com.github.manolo8.darkbot;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.DarkBotAPI;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.*;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.modules.CollectorModule;
import com.github.manolo8.darkbot.modules.LootModule;
import com.github.manolo8.darkbot.modules.LootNCollectorModule;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main extends Thread {

    public static final Object UPDATE_LOCKER = new Object();

    public static DarkBotAPI API;

    public final MapManager mapManager;
    public final StarManager starManager;
    public final HeroManager hero;
    public final GuiManager guiManager;
    public final StatsManager statsManager;
    public final PingManager pingManager;

    public final Lazy<Boolean> status;

    public Config config;
    public Module module;

    private long lastRefresh;

    private final BotInstaller botInstaller;
    private final MainGui form;

    private volatile boolean running;

    public Main() {
        API = new DarkBotAPI();
        API.createWindow();

        this.config = new Config();

        loadConfig();

        new ConfigEntity(config);

        botInstaller = new BotInstaller();
        status = new Lazy<>();

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

            sleepMax(time, 100);
        }
    }

    private void tick() {

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
        hero.tick();
        mapManager.tick();

        if (running && guiManager.canTickModule())
            tickRunning();
    }

    private void tickRunning() {
        module.tick();
        statsManager.tick();

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
        if (config.REFRESH_TIME != 0) {

            boolean refreshTimer = System.currentTimeMillis() - lastRefresh > config.REFRESH_TIME;
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

                this.config = new GsonBuilder().create().fromJson(reader, Config.class);

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

            new GsonBuilder().create().toJson(this.config, writer);

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
