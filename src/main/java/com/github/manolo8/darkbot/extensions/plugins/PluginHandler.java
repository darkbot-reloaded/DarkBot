package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.utils.AuthAPI;
import com.github.manolo8.darkbot.utils.I18n;
import com.google.gson.Gson;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
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
        if (!LISTENERS.contains(listener)) LISTENERS.add(listener);
    }

    private final Object BACKGROUND_LOCK = new Object();
    public Object getBackgroundLock() {
        return BACKGROUND_LOCK;
    }

    private File[] getJars(String folder) {
        File[] jars = new File(folder).listFiles((dir, name) -> name.endsWith(".jar"));
        return jars != null ? jars : new File[0];
    }

    /** Updates plugins asynchronously */
    public void updatePlugins() {
        Thread pluginReloader = new Thread(() -> {
            synchronized (getBackgroundLock()) {
                try {
                    SwingUtilities.invokeAndWait(this::updatePluginsInternal);
                } catch (InterruptedException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        });
        pluginReloader.setDaemon(true);
        pluginReloader.start();
    }

    public void updatePluginsSync() {
        synchronized (getBackgroundLock()) {
            updatePluginsInternal();
        }
    }

    public void updateConfig() {
        for (Plugin plugin : LOADED_PLUGINS) {
            plugin.setDefinition(plugin.getDefinition());
        }
    }

    private void updatePluginsInternal() {
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
            } catch (Throwable e) {
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
                PluginDefinition plDef = GSON.fromJson(isr, (Type) PluginDefinition.class);
                plugin.setDefinition(plDef);
            }
            testUnique(plugin);
            testCompatibility(plugin);
            testSignature(plugin, jar);
        }
    }

    private void testUnique(Plugin plugin) {
        if (LOADED_PLUGINS.stream().anyMatch(other -> other.getName().equals(plugin.getName())))
            plugin.getIssues().addFailure(I18n.get("plugins.issues.loaded_twice"),
                    I18n.get("plugins.issues.loaded_twice.desc"));
    }

    private void testCompatibility(Plugin plugin) {
        PluginDefinition pd = plugin.getDefinition();

        if (pd.minVersion.compareTo(pd.supportedVersion) > 0)
            plugin.getIssues().addFailure(I18n.get("plugins.issues.invalid_json"),
                    I18n.get("plugins.issues.invalid_json.desc", pd.minVersion, pd.supportedVersion));

        String supportedRange = "DarkBot v" + (pd.minVersion.compareTo(pd.supportedVersion) == 0 ?
                pd.minVersion : pd.minVersion + "-v" + pd.supportedVersion);

        if (Main.VERSION.compareTo(pd.minVersion) < 0)
            plugin.getIssues().addFailure(I18n.get("plugins.issues.bot_update"),
                    I18n.get("plugins.issues.bot_update.desc", supportedRange, Main.VERSION));

        if (Main.VERSION.compareTo(pd.supportedVersion) > 0)
            plugin.getIssues().addInfo(I18n.get("plugins.issues.plugin_update"),
                    I18n.get("plugins.issues.plugin_update.desc", supportedRange, Main.VERSION));
    }

    private void testSignature(Plugin plugin, JarFile jar) throws IOException {
        try {
            Boolean signatureValid = AuthAPI.getInstance().checkPluginJarSignature(jar);
            if (signatureValid == null)
                plugin.getIssues().addFailure(I18n.get("plugins.issues.signature.plugin_not_signed"),
                        I18n.get("plugins.issues.signature.plugin_not_signed.desc"));
            else if (!signatureValid)
                plugin.getIssues().addFailure(I18n.get("plugins.issues.signature.unknown_signature"),
                        I18n.get("plugins.issues.signature.unknown_signature.desc"));
        } catch (SecurityException e) {
            plugin.getIssues().addFailure(I18n.get("plugins.issues.signature.invalid_signature"),
                    I18n.get("plugins.issues.signature.invalid_signature.desc"));
        }
    }


}
