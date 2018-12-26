package com.github.manolo8.darkbot;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.DarkBotAPI;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.*;
import com.github.manolo8.darkbot.gui.MainForm;
import com.github.manolo8.darkbot.modules.CollectorModule;
import com.github.manolo8.darkbot.modules.LootModule;
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

    public Config config;
    public Module module;

    private final BotInstaller botInstaller;
    private final MainForm form;

    private volatile boolean running;

    public Main() {
        API = new DarkBotAPI();
        this.config = new Config();

        loadConfig();

        botInstaller = new BotInstaller();

        guiManager = new GuiManager(this);
        starManager = new StarManager();
        mapManager = new MapManager(this);
        hero = new HeroManager(this);
        statsManager = new StatsManager();

        botInstaller.add(guiManager);
        botInstaller.add(mapManager);
        botInstaller.add(hero);
        botInstaller.add(statsManager);

        botInstaller.init();

        form = new MainForm(this);

        updateConfig();

        start();
    }

    @Override
    public void run() {
        long time;

        while (true) {

            time = System.currentTimeMillis();

            if (botInstaller.isInvalid()) {
                botInstaller.verify();
                sleepMax(time, 5000);
            } else {

                hero.tick();
                mapManager.tick();
                guiManager.tick();

                if (running && guiManager.canTickModule()) {
                    hero.checkMove();
                    module.tick();
                    statsManager.tick();
                }

                form.tick();

                sleepMax(time, 100);
            }

        }
    }

    public <A extends Module> A setModule(A module) {
        this.module = module;
        this.module.install(this);
        return module;
    }

    public void setRunning(boolean running) {
        if (this.running != running) {

            statsManager.toggle(running);

            this.running = running;
        }
    }

    private void loadConfig() {
        try {

            File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile();


            File config = new File(file, "config.json");

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

            File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile();

            File config = new File(file, "config.json");

            if (config.exists()) {
                config.delete();
            }

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

    public void updateConfig() {
        switch (config.CURRENT_MODULE) {
            case 0:
                if (!isModule(CollectorModule.class)) {
                    setModule(new CollectorModule());
                }
                break;
            case 1:
                if (!isModule(LootModule.class)) {
                    setModule(new LootModule());
                }
                break;
        }
//        setModule(new GateModule());
    }

    private boolean isModule(Class clazz) {
        return module != null && module.getClass() == clazz;
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
