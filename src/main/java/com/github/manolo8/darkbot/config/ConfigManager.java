package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.types.suppliers.BrowserApi;
import com.github.manolo8.darkbot.config.utils.ByteArrayToBase64TypeAdapter;
import com.github.manolo8.darkbot.config.utils.ColorAdapter;
import com.github.manolo8.darkbot.config.utils.ConditionTypeAdapterFactory;
import com.github.manolo8.darkbot.config.utils.FontAdapter;
import com.github.manolo8.darkbot.config.utils.PlayerTagTypeAdapterFactory;
import com.github.manolo8.darkbot.config.utils.SpecialTypeAdapter;
import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.gui.tree.editors.FileEditor;
import com.github.manolo8.darkbot.utils.ApiErrors;
import com.github.manolo8.darkbot.utils.FileUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import eu.darkbot.api.API;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.PercentRange;
import eu.darkbot.api.config.types.ShipMode;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Responsible for loading &amp; saving configuration files
 *
 * TODO: Rename to ConfigLoader
 */
public class ConfigManager implements API.Singleton {

    private static class ApiInterfacesFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (ShipMode.class == type.getType()) {
                return gson.getAdapter((Class<T>) Config.ShipConfig.class);
            }
            if (PercentRange.class == type.getType()) {
                return gson.getAdapter((Class<T>) Config.PercentRange.class);
            }
            return null;
        }
    }

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .registerTypeAdapter(BrowserApi.class, new BrowserApi.Deserializer())
            .registerTypeAdapter(byte[].class, new ByteArrayToBase64TypeAdapter())
            .registerTypeAdapter(Color.class, new ColorAdapter())
            .registerTypeAdapter(File.class, new FileEditor.JsonAdapter())
            .registerTypeAdapter(Font.class, new FontAdapter())
            .registerTypeAdapterFactory(new SpecialTypeAdapter())
            .registerTypeAdapterFactory(new ConditionTypeAdapterFactory())
            .registerTypeAdapterFactory(new PlayerTagTypeAdapterFactory())
            .registerTypeAdapterFactory(new ApiInterfacesFactory())
            .create();

    public static final String DEFAULT = "config",
            CONFIG_FOLDER = "configs",
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
        return isDefault() ? Paths.get(getConfigName() + EXTENSION) :
                Paths.get(CONFIG_FOLDER, getConfigName() + EXTENSION);
    }

    public Path getConfigBackupFile() {
        return isDefault() ? Paths.get(getConfigName() + BACKUP + EXTENSION) :
                Paths.get(CONFIG_FOLDER, getConfigName() + BACKUP + EXTENSION);
    }

    public boolean isDefault() {
        return configName == null;
    }

    public boolean getConfigFailed() {
        return failedConfig;
    }

    public Config loadConfig(String configName) {
        if (DEFAULT.equals(configName)) configName = null;
        this.configName = configName;
        this.config = loadConfig(getConfigFile(), getConfigBackupFile());
        return this.config;
    }

    public boolean createNewConfig(String name, @Nullable String copy) {
        Path newFile = Paths.get(CONFIG_FOLDER, name + EXTENSION);
        try {
            if (copy != null) {
                Path old = DEFAULT.equals(copy) ?
                        Paths.get(copy + EXTENSION) : Paths.get(CONFIG_FOLDER, copy + EXTENSION);
                Files.copy(old, newFile);
            } else {
                Files.write(newFile, "{}".getBytes(StandardCharsets.UTF_8));
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteConfig(String name) {
        try {
            Files.deleteIfExists(Paths.get(CONFIG_FOLDER, name + EXTENSION));
            Files.deleteIfExists(Paths.get(CONFIG_FOLDER, name + BACKUP + EXTENSION));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getAvailableConfigs() {
        List<String> configs = new ArrayList<>();
        configs.add(ConfigManager.DEFAULT);

        FileUtils.ensureDirectoryExists(Paths.get(ConfigManager.CONFIG_FOLDER));
        try {
            Files.list(Paths.get(ConfigManager.CONFIG_FOLDER))
                    .sorted(Comparator.comparing(this::getLastModified).reversed())
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(n -> !n.endsWith(ConfigManager.BACKUP + ConfigManager.EXTENSION))
                    .filter(n -> n.endsWith(ConfigManager.EXTENSION))
                    .map(n -> n.substring(0, n.length() - ConfigManager.EXTENSION.length()))
                    .forEach(configs::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return configs;
    }

    private FileTime getLastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path);
        } catch (IOException e) {
            e.printStackTrace();
            return FileTime.fromMillis(0);
        }
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

        return config;
    }

    public IDarkBotAPI getAPI(PluginAPI pluginApi) {
        StartupParams params = pluginApi.requireInstance(StartupParams.class);
        BrowserApi api = params.useNoOp() ? BrowserApi.NO_OP_API : config.BOT_SETTINGS.API_CONFIG.BROWSER_API;
        try {
            if (api == null) throw new IllegalArgumentException("No API has been set!");
            return pluginApi.requireInstance(api.clazz);
        } catch (Throwable e) {
            System.out.println("Error enabling " + api + ", using no-op api");
            e.printStackTrace();
            ApiErrors.displayException(api, e);
            return pluginApi.requireInstance(BrowserApi.NO_OP_API.clazz);
        }
    }

    public void saveChangedConfig() {
        // Safe config 5s after last change, to batch-up changes
        if (!config.changed || config.changedAt + 5000 > System.currentTimeMillis()) return;
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
