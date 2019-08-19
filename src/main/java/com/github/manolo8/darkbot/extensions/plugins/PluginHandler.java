package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.util.SignatureChecker;
import com.google.gson.Gson;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class PluginHandler {
    private static final Gson GSON = new Gson();

    public static final File PLUGIN_FOLDER = new File("plugins"),
            PLUGIN_UPDATE_FOLDER = new File("plugins/updates");
    public static final Path PLUGIN_PATH = PLUGIN_FOLDER.toPath(),
            PLUGIN_UPDATE_PATH = PLUGIN_UPDATE_FOLDER.toPath();

    public URLClassLoader PLUGIN_CLASS_LOADER;
    public List<Plugin> LOADED_PLUGINS = new ArrayList<>();
    public List<Plugin> FAILED_PLUGINS = new ArrayList<>();
    public List<PluginLoadingException> LOADING_EXCEPTIONS = new ArrayList<>();

    private static List<PluginListener> LISTENERS = new ArrayList<>();

    public void addListener(PluginListener listener) {
        LISTENERS.add(listener);
    }

    private File[] getJars(String folder) {
        File[] jars = new File(folder).listFiles((dir, name) -> name.endsWith(".jar"));
        return jars != null ? jars : new File[0];
    }

    public void updatePlugins() {
        SwingUtilities.invokeLater(this::updatePluginsSync);
    }

    public void updatePluginsSync() {
        synchronized (this) {
            LOADED_PLUGINS.clear();
            FAILED_PLUGINS.clear();
            LOADING_EXCEPTIONS.clear();
            LISTENERS.forEach(PluginListener::beforeLoad);
            System.gc();

            if (PLUGIN_CLASS_LOADER != null) {
                try {
                    PLUGIN_CLASS_LOADER.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            for (File plugin : getJars("plugins/updates")) {
                Path plPath = plugin.toPath();
                try {
                    Files.move(plPath, PLUGIN_PATH.resolve(plPath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    LOADING_EXCEPTIONS.add(new PluginLoadingException("Failed to update plugin: " + plPath.getFileName(), e));
                    e.printStackTrace();
                }
            }
            try {
                loadPlugins(getJars("plugins"));
            } catch (Exception e) {
                LOADING_EXCEPTIONS.add(new PluginLoadingException("Failed to load plugins", e));
                e.printStackTrace();
            }
            LISTENERS.forEach(PluginListener::afterLoad);
        }
        LISTENERS.forEach(PluginListener::afterLoadComplete);
    }

    private void loadPlugins(File[] pluginFiles) {
        for (File pluginFile : pluginFiles) {
            Plugin pl = null;
            try {
                pl = new Plugin(pluginFile, pluginFile.toURI().toURL());
                loadPlugin(pl);
                if (pl.getIssues().canLoad()) LOADED_PLUGINS.add(pl);
                else FAILED_PLUGINS.add(pl);
            } catch (PluginLoadingException e) {
                LOADING_EXCEPTIONS.add(e);
                e.printStackTrace();
            } catch (Exception e) {
                LOADING_EXCEPTIONS.add(new PluginLoadingException("Failed to load plugin", e, pl));
                e.printStackTrace();
            }
        }
        PLUGIN_CLASS_LOADER = new URLClassLoader(LOADED_PLUGINS.stream().map(Plugin::getJar).toArray(URL[]::new));
    }

    private void loadPlugin(Plugin plugin) throws IOException, PluginLoadingException {
        try (JarFile jar = new JarFile(plugin.getFile(), true)) {
            ZipEntry plJson = jar.getEntry("plugin.json");
            if (plJson == null) {
                throw new PluginLoadingException("The plugin is missing a plugin.json in the jar root", plugin);
            }
            try (InputStreamReader isr = new InputStreamReader(jar.getInputStream(plJson), StandardCharsets.UTF_8)) {
                PluginDefinition plDef = GSON.fromJson(isr, PluginDefinition.class);
                plugin.setDefinition(plDef);
            }

            testCompatibility(plugin);
            testSignature(plugin, jar);
        }
    }

    private void testCompatibility(Plugin plugin) {
        PluginDefinition pd = plugin.getDefinition();

        if (pd.minVersion.compareTo(pd.supportedVersion) > 0)
            plugin.getIssues().addFailure("Invalid plugin.json",
                    "The minimum version " + pd.minVersion  + " is higher than the supported version " + pd.supportedVersion);

        String supportedRange = "DarkBot v" + (pd.minVersion.compareTo(pd.supportedVersion) == 0 ?
                pd.minVersion : pd.minVersion + "-v" + pd.supportedVersion);

        if (Main.VERSION.compareTo(pd.minVersion) < 0)
            plugin.getIssues().addFailure("Invalid min version",
                    "This plugin requires " + supportedRange + ", so it can't run on Darkbot v" + Main.VERSION);

        if (Main.VERSION.compareTo(pd.supportedVersion) > 0)
            plugin.getIssues().addWarning("Plugin may need update",
                    "The plugin is made for " + supportedRange + ", so it may not work on DarkBot v" + Main.VERSION);
    }

    private void testSignature(Plugin plugin, JarFile jar) throws IOException {
        try {
            if (!SignatureChecker.verifyJar(jar)) {
                plugin.getIssues().addWarning("Plugin not signed",
                        "This plugin hasn't been signed or has an invalid signature");
            }
        } catch (SecurityException e) {
            plugin.getIssues().addFailure("Invalid signature", "The plugin has an invalid signature or has been tampered with");
        }
    }


}
