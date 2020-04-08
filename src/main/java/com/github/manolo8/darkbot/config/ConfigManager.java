package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.utils.ByteArrayToBase64TypeAdapter;
import com.github.manolo8.darkbot.config.utils.SpecialTypeAdapter;
import com.github.manolo8.darkbot.core.DarkBotAPI;
import com.github.manolo8.darkbot.core.DarkFlash;
import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.utils.LoginUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ConfigManager {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter())
            .registerTypeAdapterFactory(new SpecialTypeAdapter())
            .create();

    private static final String DEFAULT = "config",
            BACKUP = "_old",
            EXTENSION = ".json";

    private String configName;
    private Config config;
    private boolean failedConfig;

    public String getConfigName() {
        return this.configName == null ? DEFAULT : this.configName;
    }

    public Config getConfig() {
        return this.config;
    }

    public Path getConfigFile() {
        return Paths.get(getConfigName() + EXTENSION);
    }

    public Path getConfigBackupFile() {
        return Paths.get(getConfigName() + BACKUP + EXTENSION);
    }

    public boolean getConfigFailed() {
        return failedConfig;
    }

    public Config loadConfig(String configName) {
        this.configName = configName;
        this.config = loadConfig(getConfigFile(), getConfigBackupFile());
        return this.config;
    }


    private Config loadConfig(Path configFile, Path backupFile) {
        boolean existsConfig = Files.exists(configFile),
                existsBackup = Files.exists(backupFile);

        Config config = null;

        if (existsConfig) config = loadConfig(configFile);

        if (existsBackup && config == null) {
            config = loadConfig(backupFile);
            if (config != null)
                saveConfig(config, configFile, backupFile);
        }

        failedConfig = config == null && (existsConfig || existsBackup);

        if (config == null) {
            config = new Config();
            // Didn't have any previous config, save this new one:
            if (!failedConfig) saveConfig(config, configFile, backupFile);
        }
        ConfigEntity.INSTANCE.setConfig(config);

        return config;
    }

    public IDarkBotAPI getAPI() {
        if (config.BOT_SETTINGS.API == 0) return new DarkBotAPI();
        else if (config.BOT_SETTINGS.API == 1) return new DarkFlash(new LoginUtils().performSidLogin().getLoginData());
        else throw new IllegalArgumentException("API not found: " + config.BOT_SETTINGS.API);
    }

    public void checkConfig() {
        if (!config.changed) return;
        config.changed = false;
        saveConfig();
    }

    private Config loadConfig(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            return GSON.fromJson(reader, Config.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveConfig() {
        if (failedConfig) return; // Don't save defaults if config failed to load!
        saveConfig(getConfig(), getConfigFile(), getConfigBackupFile());
    }

    private static void saveConfig(Config config, Path configFile, Path backupFile) {
        if (Files.exists(configFile)) {
            try {
                Files.move(configFile, backupFile,
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                System.err.println("Couldn't move config before updating save file");
                e.printStackTrace();
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
