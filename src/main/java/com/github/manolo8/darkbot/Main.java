package com.github.manolo8.darkbot;

import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.utils.ByteArrayToBase64TypeAdapter;
import com.github.manolo8.darkbot.config.utils.SpecialTypeAdapter;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.DarkBotAPI;
import com.github.manolo8.darkbot.core.DarkFlash;
import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.core.itf.Behaviour;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.GuiManager;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.manager.MediatorManager;
import com.github.manolo8.darkbot.core.manager.PingManager;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.core.manager.GroupManager;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginListener;
import com.github.manolo8.darkbot.extensions.util.VerifierChecker;
import com.github.manolo8.darkbot.extensions.util.Version;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.modules.DisconnectModule;
import com.github.manolo8.darkbot.modules.DummyModule;
import com.github.manolo8.darkbot.modules.TemporalModule;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.LoginUtils;
import com.github.manolo8.darkbot.utils.Time;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Main extends Thread implements PluginListener {

    public static final String VERSION_STRING = "1.13.17 alpha 3";
    public static final Version VERSION = new Version(VERSION_STRING);

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter())
            .registerTypeAdapterFactory(new SpecialTypeAdapter())
            .create();

    public static final Object UPDATE_LOCKER = new Object();

    public static IDarkBotAPI API;

    public final MapManager mapManager;
    public final StarManager starManager;
    public final HeroManager hero;
    public final MediatorManager mediatorManager;
    public final GuiManager guiManager;
    public final StatsManager statsManager;
    public final PingManager pingManager;
    public final BackpageManager backpage;

    public final Lazy.Sync<Boolean> status;

    public Config config;
    private boolean failedConfig;

    public final PluginHandler pluginHandler;
    public final FeatureRegistry featureRegistry;
    private List<Behaviour> behaviours = new ArrayList<>();
    private String moduleId;
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
        new ConfigEntity(config);

        VerifierChecker.getAuthApi().setupAuth();

        if (config.BOT_SETTINGS.API == 0) API = new DarkBotAPI();
        else if (config.BOT_SETTINGS.API == 1) API = new DarkFlash(new LoginUtils().performSidLogin().getLoginData());
        //else if (config.API == 2) API = new
        else throw new IllegalArgumentException("API not found: " + config.BOT_SETTINGS.API);

        botInstaller = new BotInstaller();
        status = new Lazy.Sync<>();

        starManager = new StarManager();
        mapManager = new MapManager(this);
        hero = new HeroManager(this);
        mediatorManager = new MediatorManager(this);
        guiManager = new GuiManager(this);
        statsManager = new StatsManager(this);
        pingManager = new PingManager();

        botInstaller.add(mediatorManager);
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

        backpage = new BackpageManager(this);

        pluginHandler = new PluginHandler();
        featureRegistry = new FeatureRegistry(this, pluginHandler);

        pluginHandler.updatePluginsSync();
        pluginHandler.addListener(this);

        form = new MainGui(this);

        if (failedConfig) Popups.showMessageAsync("Error", I18n.get("bot.issue.config_load_failed"), JOptionPane.ERROR_MESSAGE);

        API.createWindow();
        start();
    }

    @Override
    public void run() {
        long time;

        while (true) {
            time = System.currentTimeMillis();

            try {
                tick();
            } catch (Exception e) {
                e.printStackTrace();
                Time.sleep(1000);
            }

            double tickTime = System.currentTimeMillis() - time;
            avgTick = ((avgTick * 9) + tickTime) / 10;

            sleepMax(time, botInstaller.invalid.value ? 1000 :
                    Math.max(config.BOT_SETTINGS.MIN_TICK, Math.min((int) (avgTick * 1.25), 100)));
        }
    }

    private void tick() {
        status.tick();
        checkModule();

        if (isInvalid())
            invalidTick();
        else
            validTick();

        form.tick();

        checkConfig();
    }

    private boolean isInvalid() {
        return botInstaller.isInvalid();
    }

    private void invalidTick() {
        tickingModule = false;
        botInstaller.verify();
    }

    private void validTick() {
        hero.tick();
        mapManager.tick();
        mediatorManager.tick();
        guiManager.tick();
        statsManager.tick();

        tickingModule = running && guiManager.canTickModule();
        if (running && guiManager.canTickModule()) tickRunning();
        else tickLogic(false);

        if (!running && (!hero.hasTarget() || !mapManager.isTarget(hero.target))) {
            hero.setTarget(Stream.concat(mapManager.entities.ships.stream(), mapManager.entities.npcs.stream())
                    .filter(mapManager::isTarget)
                    .findFirst().orElse(null));
        }

        pingManager.tick();
    }

    private void tickRunning() {
        guiManager.pet.tick();
        guiManager.group.tick();
        checkRefresh();
        tickLogic(true);
        checkPetBug();
    }


    private void tickLogic(boolean running) {
        synchronized (pluginHandler) {
            try {
                if (running) module.tick();
                else module.tickStopped();
            } catch (Throwable e) {
                FeatureDefinition<Module> modDef = featureRegistry.getFeatureDefinition(module);
                if (modDef != null) modDef.getIssues().addWarning(I18n.get("bot.issue.feature.failed_to_tick"), IssueHandler.createDescription(e));
            }
            for (Behaviour behaviour : behaviours) {
                try {
                    if (running) behaviour.tick();
                    else behaviour.tickStopped();
                } catch (Throwable e) {
                    featureRegistry.getFeatureDefinition(behaviour)
                            .getIssues()
                            .addFailure(I18n.get("bot.issue.feature.failed_to_tick"), IssueHandler.createDescription(e));
                }
            }
        }
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
        lastRefresh = System.currentTimeMillis();
        if (config.MISCELLANEOUS.PAUSE_FOR > 0) {
            System.out.println("Pausing (logging off): time arrived & module allows refresh");
            setModule(new DisconnectModule(config.MISCELLANEOUS.PAUSE_FOR * 60 * 1000L, I18n.get("module.disconnect.reason.break")));
        } else {
            System.out.println("Triggering refresh: time arrived & module allows refresh");
            API.handleRefresh();
        }
    }

    public <A extends Module> A setModule(A module) {
        return setModule(module, false);
    }

    private <A extends Module> A setModule(A module, boolean setConfig) {
        module.install(this);
        if (setConfig) {
            if (module instanceof Configurable) {
                String name = module.getClass().getAnnotation(Feature.class).name();
                form.setCustomConfig(name, config.CUSTOM_CONFIGS.get(module.getClass().getCanonicalName()));
            } else {
                form.setCustomConfig(null, null);
            }
        }
        this.module = module;
        return module;
    }

    @Override
    public void beforeLoad() {
        if (module != null) setModule(new DummyModule(), true);
    }

    @Override
    public void afterLoadComplete() {
        moduleId = "(none)";
    }

    public void setRunning(boolean running) {
        if (this.running == running) return;
        status.send(running);
        this.running = running;
    }

    private void onRunningToggle(boolean running) {
        lastRefresh = System.currentTimeMillis();
        if (running && module instanceof TemporalModule) {
            moduleId = "(none)";
        }
    }

    private void loadConfig() {
        File config = new File("config.json");
        if (!config.exists()) {
            saveConfig();
            return;
        }
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(config), StandardCharsets.UTF_8)) {
            this.config = GSON.fromJson(reader, Config.class);
            if (this.config == null) this.config = new Config();
        } catch (Exception e) {
            failedConfig = true;
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        if (failedConfig) return; // Don't save defaults if config failed to load!
        File config = new File("config.json");
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(config), StandardCharsets.UTF_8)) {
            GSON.toJson(this.config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setBehaviours(List<Behaviour> behaviours) {
        this.behaviours = behaviours;
    }

    private void checkModule() {
        if (module == null || !Objects.equals(moduleId, config.GENERAL.CURRENT_MODULE)) {
            Module module = featureRegistry.getFeature(moduleId = config.GENERAL.CURRENT_MODULE, Module.class)
                .orElseGet(() -> {
                    String name = moduleId.substring(moduleId.lastIndexOf(".") + 1);
                    Popups.showMessageAsync("Error", I18n.get("bot.issue.module_load_failed", name), JOptionPane.ERROR_MESSAGE);
                    return new DummyModule();
                });
            setModule(module, true);
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
