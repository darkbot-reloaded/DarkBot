package com.github.manolo8.darkbot;

import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.utils.ByteArrayToBase64TypeAdapter;
import com.github.manolo8.darkbot.config.utils.SpecialTypeAdapter;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.DarkBotAPI;
import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.extensions.behaviours.BehaviourHandler;
import com.github.manolo8.darkbot.extensions.modules.ConfigurableModule;
import com.github.manolo8.darkbot.extensions.modules.CustomModule;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.GuiManager;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.manager.PingManager;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.extensions.modules.ModuleHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.modules.MapModule;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
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
import java.util.stream.Stream;

public class Main extends Thread {

    public static final String VERSION = "1.13.13 beta 7";

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
    public final GuiManager guiManager;
    public final StatsManager statsManager;
    public final PingManager pingManager;
    public final BackpageManager backpage;

    public final Lazy.Sync<Boolean> status;

    public Config config;
    private boolean failedConfig;

    private final PluginHandler pluginHandler;
    private final ModuleHandler moduleHandler;
    private final BehaviourHandler behaviourHandler;
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

        API = new DarkBotAPI(config);
        /*if (config.MISCELLANEOUS.FULL_DEBUG)
            API = (IDarkBotAPI) Proxy.newProxyInstance(Main.class.getClassLoader(), new Class[]{IDarkBotAPI.class}, IDarkBotAPI.getLoggingHandler((DarkBotAPI) API));
        */

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

        backpage = new BackpageManager(this);

        pluginHandler = new PluginHandler();
        moduleHandler = new ModuleHandler(pluginHandler);
        behaviourHandler = new BehaviourHandler(pluginHandler);

        pluginHandler.updatePlugins();

        form = new MainGui(this);

        if (failedConfig) Popups.showMessageAsync("Error",
                "Failed to load config. Default config will be used, config won't be save.", JOptionPane.ERROR_MESSAGE);

        checkModule();
        start();
        API.createWindow();
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
                    Math.max(config.MISCELLANEOUS.MIN_TICK, Math.min((int) (avgTick * 1.25), 100)));
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
        else if (!running && (!hero.hasTarget() || !mapManager.isTarget(hero.target))) {
            hero.setTarget(Stream.concat(mapManager.entities.ships.stream(), mapManager.entities.npcs.stream())
                    .filter(mapManager::isTarget)
                    .findFirst().orElse(null));
        }

        pingManager.tick();
        //if (config.MISCELLANEOUS.DEV_STUFF && mapManager.width > 0)
        //    API.pixelsAndDisplay(0, 0, (int) mapManager.width, (int) mapManager.height);
    }

    private void tickRunning() {
        guiManager.pet.tick();
        checkRefresh();
        module.tick();
        checkPetBug();
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
        System.out.println("Triggering refresh: time arrived & module allows refresh");
        API.handleRefresh();
        lastRefresh = System.currentTimeMillis();
    }

    public <A extends Module> A setModule(A module) {
        module.install(this);
        if (module instanceof ConfigurableModule) installCustomModule((ConfigurableModule<?>) module);
        else form.setCustomConfig(null, null);
        this.module = module;
        return module;
    }

    private void installCustomModule(ConfigurableModule module) {
        CustomModule customMod = module.getClass().getAnnotation(CustomModule.class);
        Class<?> configClass;
        Object moduleConfig = null;
        if (customMod != null && (configClass = customMod.configuration()) != Void.class) {
            Object storedConfig = config.CUSTOM_CONFIGS.get(module.getClass().getCanonicalName());

            moduleConfig = storedConfig != null ? Main.GSON.fromJson(Main.GSON.toJsonTree(storedConfig), configClass) :
                            ReflectionUtils.createInstance(configClass);
            config.CUSTOM_CONFIGS.put(module.getClass().getCanonicalName(), moduleConfig);

            form.setCustomConfig(customMod.name(), moduleConfig);
        } else {
            form.setCustomConfig(null, null);
        }
        //noinspection unchecked
        module.install(this, moduleConfig);
    }

    public void setRunning(boolean running) {
        if (this.running == running) return;
        status.send(running);
        this.running = running;
    }

    private void onRunningToggle(boolean running) {
        lastRefresh = System.currentTimeMillis();
        if (running && module instanceof MapModule) {
            moduleId = null;
            checkModule();
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

    private void checkModule() {
        if (module == null || moduleId == null || !moduleId.equals(config.GENERAL.CURRENT_MODULE))
            setModule(moduleHandler.getFeature(moduleId = config.GENERAL.CURRENT_MODULE));
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
