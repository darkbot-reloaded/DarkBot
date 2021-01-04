package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.utils.ByteArrayToBase64TypeAdapter;
import com.github.manolo8.darkbot.config.utils.SpecialTypeAdapter;
import com.github.manolo8.darkbot.core.IDarkBotAPI;
import com.github.manolo8.darkbot.core.api.DarkBoatAdapter;
import com.github.manolo8.darkbot.core.api.DarkBotApiAdapter;
import com.github.manolo8.darkbot.core.api.DarkFlashApiAdapter;
import com.github.manolo8.darkbot.core.api.NativeApiAdapter;
import com.github.manolo8.darkbot.core.api.NoopApiAdapter;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.StartupParams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.github.manolo8.darkbot.gui.utils.Popups.showMessageSync;

public class ConfigManager {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter())
            .registerTypeAdapterFactory(new SpecialTypeAdapter())
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

    public IDarkBotAPI getAPI(StartupParams params) {
        if (params.useNoOp()) return new NoopApiAdapter();
        try {
            if (config.BOT_SETTINGS.API_CONFIG.API == 0) return new DarkBotApiAdapter();
            else if (config.BOT_SETTINGS.API_CONFIG.API == 1) return new DarkFlashApiAdapter(params);
            else if (config.BOT_SETTINGS.API_CONFIG.API == 2) return new DarkBoatAdapter(params);
            else if (config.BOT_SETTINGS.API_CONFIG.API == 3) return new NativeApiAdapter(params);
            else if (config.BOT_SETTINGS.API_CONFIG.API == 4) return new NoopApiAdapter();
            else throw new IllegalArgumentException("API not found: " + config.BOT_SETTINGS.API_CONFIG.API);
        } catch (Error e) {
            JPanel pnl = new JPanel();

            boolean notApiRelated = false;
            String workingDir     = System.getProperty("user.dir");

            if(e.getMessage().contains("not found in resource path")) {
                String arch      = System.getenv("PROCESSOR_ARCHITECTURE");
                String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

                String os_arch = arch != null && arch.endsWith("64")
                        || wow64Arch != null && wow64Arch.endsWith("64")
                        ? "64" : "32";

                if (os_arch.equals("32")) {
                    String msg = "<html>The bot requires a <b>64-bit</b> operating system<br /> " +
                            "It's not possible to run DarkBot on a <b>32-bit</b> OS!<br /><br />";
                    pnl.add(new JLabel(msg));
                } else {
                    String rnt = "https://darkbot.eu/downloads/Runtimes4DarkBot.exe";
                    String msg = "<html>You tried to run the bot on a <b>32-bit</b> Java, but a <b>64-bit</b> one is required!<br/>" +
                            "To quickly fix this issue, consider installing our runtime:";
                    String dwn = "<html><p>Download link: <a href=\"" + rnt +"\">" + rnt + "</a>";
                    JLabel dwnLink = new JLabel(dwn);
                    dwnLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    dwnLink.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            try {
                                Desktop.getDesktop().browse(new URI(rnt));
                            } catch (URISyntaxException | IOException ignored) {}
                        }
                    });
                    pnl.setLayout(new GridLayout( 2 , 1 ));
                    pnl.add(new JLabel(msg));
                    pnl.add(dwnLink);
                }
                notApiRelated = true;
            } else if(!workingDir.matches("[\\x00-\\x7F]+")) {

                String[] folderNames = workingDir.split("\\\\");
                String charPointers  = workingDir
                        .replaceAll("[\\x00-\\x7F]", "&nbsp")
                        .replaceAll("[^\\x00-\\x7F]", "^");
                StringBuilder path = new StringBuilder("<p style=\"font-family: Consolas\">");

                for (String name : folderNames) {
                    path.append(!name.matches("[\\x00-\\x7F]+")
                            ? "<font color=\"#ff7d7d\">" + name + "/</font>"
                            : name + '/'
                    );
                }

                String msg = "<html>The bot folder path contains <b>non-ANSI</b> characters. Rename the folders<br/>"
                        + "where they occur or move the bot folder to <b>D:\\</b> and then run again<br/><br/>"
                        +  path.toString() + "<br/>" + charPointers;

                pnl.add(new JLabel(msg));
                notApiRelated = true;
            }
            if (notApiRelated) {
                JOptionPane options = new JOptionPane(pnl,
                        JOptionPane.ERROR_MESSAGE,
                        JOptionPane.DEFAULT_OPTION,
                        null, new Object[]{}, null
                );
                showMessageSync("Error", options);
                System.exit(0);
            }
            System.out.println("Error enabling API #" + config.BOT_SETTINGS.API_CONFIG.API + ", using no-op api");
            e.printStackTrace();
            config.BOT_SETTINGS.API_CONFIG.API = 4;
            Popups.showMessageAsync(
                    "API failed to load",
                    "The API you had selected is not able to load.\n" +
                    "You probably do not have the required DLL in your lib folder.\n" +
                    "The bot will start on no-operation API, change it in the settings and restart.",
                    JOptionPane.ERROR_MESSAGE);
            return new NoopApiAdapter();
        }
    }

    public void saveChangedConfig() {
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
