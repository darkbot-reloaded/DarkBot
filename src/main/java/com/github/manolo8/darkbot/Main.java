package com.github.manolo8.darkbot;

import com.bulenkov.darcula.DarculaLaf;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.IDarkBotAPI;
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
import com.github.manolo8.darkbot.modules.MapModule;
import com.github.manolo8.darkbot.utils.ByteArrayToBase64TypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Proxy;

public class Main extends Thread {
    public static final String VERSION = "1.13.6beta 3";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).create();

    public static final Object UPDATE_LOCKER = new Object();

    public static IDarkBotAPI API;

    public final MapManager mapManager;
    public final StarManager starManager;
    public final HeroManager hero;
    public final GuiManager guiManager;
    public final StatsManager statsManager;
    public final PingManager pingManager;
    public final BackpageManager backpage;

    public final Lazy.Sync<Boolean> status;

    public Config config;
    private int lastModule;
    public Module module;

    public long lastRefresh;

    private final BotInstaller botInstaller;
    private final MainGui form;

    public double avgTick;

    private volatile boolean running;
    public boolean tickingModule;

    public Main() {
        super("Main");
        this.config = new Config();
        loadConfig();

        API = new DarkBotAPI(config);
        if (config.MISCELLANEOUS.FULL_DEBUG)
            API = (IDarkBotAPI) Proxy.newProxyInstance(Main.class.getClassLoader(), new Class[]{IDarkBotAPI.class}, IDarkBotAPI.getLoggingHandler((DarkBotAPI) API));

        if (config.MISCELLANEOUS.DISPLAY.USE_DARCULA_THEME) {
            try {
                UIManager.setLookAndFeel(new DarculaLaf());
            } catch (UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
        }

        new ConfigEntity(config);

        botInstaller = new BotInstaller();
        status = new Lazy.Sync<>();

        starManager = new StarManager();
        mapManager = new MapManager(this);
        hero = new HeroManager(this);
        guiManager = new GuiManager(this);
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

        status.add(this::onRunningToggle);
        checkModule();

        form = new MainGui(this);
        backpage = new BackpageManager(this);

        start();
        API.createWindow();
    }

    @Override
    public void run() {
        long time;

        while (true) {
            time = System.currentTimeMillis();

            tick();

            double tickTime = System.currentTimeMillis() - time;
            avgTick = ((avgTick * 9) + tickTime) / 10;
            sleepMax(time, botInstaller.invalid.value ? 1000 : 100);
        }
    }

    private void tick() {
        status.tick();

        if (isInvalid())
            invalidTick();
        else
            validTick();

        form.tick();

        checkConfig();
        checkModule();
    }

    private boolean isInvalid() {
        return botInstaller.isInvalid();
    }

    private void invalidTick() {
        tickingModule = false;
        botInstaller.verify();
    }

    private void validTick() {
        guiManager.tick();
        hero.tick();
        mapManager.tick();
        statsManager.tick();

        tickingModule = running && guiManager.canTickModule();
        if (tickingModule) tickRunning();

        pingManager.tick();
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
        if (config.MISCELLANEOUS.REFRESH_TIME == 0 ||
                System.currentTimeMillis() - lastRefresh < config.MISCELLANEOUS.REFRESH_TIME * 60 * 1000) return;

        if (!module.canRefresh()) return;
        API.handleRefresh();
        lastRefresh = System.currentTimeMillis();
    }

    public <A extends Module> A setModule(A module) {
        module.install(this);
        this.module = module;
        return module;
    }

    public void setRunning(boolean running) {
        if (this.running == running) return;
        status.send(running);
        this.running = running;
    }

    private void onRunningToggle(boolean running) {
        lastRefresh = System.currentTimeMillis();
        if (running && module instanceof MapModule) checkModule();
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

    private void checkModule() {
        if (module == null || lastModule != config.GENERAL.CURRENT_MODULE)
            setModule(getModule(lastModule = config.GENERAL.CURRENT_MODULE));
    }

    private Module getModule(int id) {
        switch (id) {
            case 0: return new CollectorModule();
            case 1: return new LootModule();
            case 2: return new LootNCollectorModule();
            case 3: return new EventModule();
            default: return new CollectorModule();
        }
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
