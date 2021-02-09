package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.utils.AuthAPI;
import com.github.manolo8.darkbot.utils.FileUtils;
import com.github.manolo8.darkbot.utils.I18n;
import com.google.gson.Gson;
import eu.darkbot.api.API;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public class PluginHandler implements API.Singleton {
    private static final Gson GSON = new Gson();

    public static final PluginIssue LOADED_TWICE = new PluginIssue("plugins.issues.loaded_twice",
            I18n.get("plugins.issues.loaded_twice.desc"), PluginIssue.Level.ERROR);
    public static final PluginIssue UPDATE_NOT_POSSIBLE = new PluginIssue("plugins.update_issues.updates_not_possible",
            I18n.get("plugins.update_issues.updates_not_possible.desc"), PluginIssue.Level.INFO);
    public static final String INVALID_UPDATE_JSON = "plugins.update_issues.invalid_json";
    public static final String INVALID_JSON = "plugins.issues.invalid_json";
    public static final String BOT_UPDATE_REQUIRED = "plugins.update_issues.bot_update";
    public static final String BOT_UPDATE = "plugins.issues.bot_update";
    public static final String MAY_NEED_UPDATE = "plugins.issues.plugin_update";
    public static final PluginIssue PLUGIN_NOT_SIGNED = new PluginIssue("plugins.issues.signature.plugin_not_signed",
            I18n.get("plugins.issues.signature.plugin_not_signed.desc"), PluginIssue.Level.ERROR);
    public static final PluginIssue UNKNOWN_SIGNATURE = new PluginIssue("plugins.issues.signature.unknown_signature",
            I18n.get( "plugins.issues.signature.unknown_signature.desc"), PluginIssue.Level.ERROR);
    public static final PluginIssue INVALID_SIGNATURE = new PluginIssue("plugins.issues.signature.invalid_signature",
            I18n.get("plugins.issues.signature.invalid_signature.desc"), PluginIssue.Level.ERROR);

    public static final File PLUGIN_FOLDER = new File("plugins"),
            PLUGIN_UPDATE_FOLDER = new File("plugins/updates"),
            PLUGIN_OLD_FOLDER    = new File("plugins/old");
    public static final Path PLUGIN_PATH = PLUGIN_FOLDER.toPath(),
            PLUGIN_UPDATE_PATH = PLUGIN_UPDATE_FOLDER.toPath(),
            PLUGIN_OLD_PATH    = PLUGIN_OLD_FOLDER.toPath();

    public URLClassLoader PLUGIN_CLASS_LOADER;
    public final List<Plugin> LOADED_PLUGINS = new ArrayList<>();
    public final List<Plugin> FAILED_PLUGINS = new ArrayList<>();
    public final List<PluginException> LOADING_EXCEPTIONS = new ArrayList<>();

    private static final List<PluginListener> LISTENERS = new ArrayList<>();

    public void addListener(PluginListener listener) {
        if (!LISTENERS.contains(listener)) LISTENERS.add(listener);
    }

    private final Object BACKGROUND_LOCK = new Object();
    public Object getBackgroundLock() {
        return BACKGROUND_LOCK;
    }

    private File[] getJars(File folder) {
        File[] jars = folder.listFiles((dir, name) -> name.endsWith(".jar"));
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
            List<Plugin> previousPlugins = new ArrayList<>(LOADED_PLUGINS);

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

            FileUtils.ensureDirectoryExists(PLUGIN_PATH);
            for (File plugin : getJars(PLUGIN_UPDATE_FOLDER)) {
                Path plPath = plugin.toPath();
                try {
                    Files.move(plPath, PLUGIN_PATH.resolve(plPath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    LOADING_EXCEPTIONS.add(new PluginException("Failed to update plugin: " + plPath.getFileName(), e));
                    e.printStackTrace();
                }
            }
            try {
                loadPlugins(getJars(PLUGIN_FOLDER), previousPlugins);
            } catch (Exception e) {
                LOADING_EXCEPTIONS.add(new PluginException("Failed to load plugins", e));
                e.printStackTrace();
            }
            LISTENERS.forEach(PluginListener::afterLoad);
        }
        LISTENERS.forEach(PluginListener::afterLoadComplete);
    }

    public Stream<Plugin> getAvailableUpdates() {
        return LOADED_PLUGINS.stream()
                .filter(pl -> pl.getUpdateStatus() == Plugin.UpdateStatus.AVAILABLE);
    }

    private void loadPlugins(File[] pluginFiles, List<Plugin> previousPlugins) {
        for (File pluginFile : pluginFiles) {
            Plugin pl = null;
            try {
                pl = new Plugin(pluginFile, pluginFile.toURI().toURL());
                loadPlugin(pl);

                // need to copy over previous update status and update issues or else they will be lost
                int prevIndex = previousPlugins.indexOf(pl);
                if (prevIndex != -1) {
                    Plugin plugin = previousPlugins.get(prevIndex);
                    pl.setUpdateStatus(plugin.getUpdateStatus());
                    plugin.getUpdateIssues().getIssues().forEach(pl.getUpdateIssues()::add);
                    pl.setUpdateDefinition(plugin.getUpdateDefinition());
                }

                if (pl.getIssues().canLoad()) LOADED_PLUGINS.add(pl);
                else FAILED_PLUGINS.add(pl);
            } catch (PluginException e) {
                LOADING_EXCEPTIONS.add(e);
                e.printStackTrace();
            } catch (Throwable e) {
                LOADING_EXCEPTIONS.add(new PluginException("Failed to load plugin", e, pl));
                e.printStackTrace();
            }
        }
        PLUGIN_CLASS_LOADER = new URLClassLoader(LOADED_PLUGINS.stream().map(Plugin::getJar).toArray(URL[]::new));
    }

    private void loadPlugin(Plugin plugin) throws IOException, PluginException {
        try (JarFile jar = new JarFile(plugin.getFile(), true)) {
            ZipEntry plJson = jar.getEntry("plugin.json");
            if (plJson == null) {
                throw new PluginException("The plugin is missing a plugin.json in the jar root", plugin);
            }
            plugin.setDefinition(readPluginDefinition(jar.getInputStream(plJson)));
            testUnique(plugin);
            testCompatibility(plugin);
            testSignature(plugin, jar);
        }
    }

    public PluginDefinition readPluginDefinition(InputStream is) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return GSON.fromJson(isr, (Type) PluginDefinition.class);
        }
    }

    private void testUnique(Plugin plugin) {
        if (LOADED_PLUGINS.stream().anyMatch(other -> other.getName().equals(plugin.getName())))
            plugin.getIssues().add(LOADED_TWICE);
    }

    private void testCompatibility(Plugin plugin) {
        testCompatibility(plugin.getIssues(), plugin.getDefinition(), false);
    }

    void testCompatibility(IssueHandler issues, PluginDefinition pd, boolean isUpdate) {
        if (isUpdate && (pd.download == null || pd.update == null))
            issues.add(UPDATE_NOT_POSSIBLE);

        if (pd.minVersion.compareTo(pd.supportedVersion) > 0)
            issues.addFailure((isUpdate ? INVALID_UPDATE_JSON : INVALID_JSON),
                    I18n.get("plugins.issues.invalid_json.desc", pd.minVersion, pd.supportedVersion));

        String supportedRange = "DarkBot v" + (pd.minVersion.compareTo(pd.supportedVersion) == 0 ?
                pd.minVersion : pd.minVersion + "-v" + pd.supportedVersion);

        if (Main.VERSION.compareTo(pd.minVersion) < 0)
            issues.addFailure((isUpdate ? BOT_UPDATE_REQUIRED : BOT_UPDATE),
                    I18n.get(isUpdate ? "plugins.update_issues.bot_update.desc" : "plugins.issues.bot_update.desc",
                            supportedRange, Main.VERSION));

        if (!isUpdate && Main.VERSION.compareTo(pd.supportedVersion) > 0)
            issues.addInfo(MAY_NEED_UPDATE, I18n.get("plugins.issues.plugin_update.desc", supportedRange, Main.VERSION));
    }

    private void testSignature(Plugin plugin, JarFile jar) throws IOException {
        try {
            Boolean signatureValid = AuthAPI.getInstance().checkPluginJarSignature(jar);
            if (signatureValid == null) plugin.getIssues().add(PLUGIN_NOT_SIGNED);
            else if (!signatureValid) plugin.getIssues().add(UNKNOWN_SIGNATURE);
        } catch (SecurityException e) {
            plugin.getIssues().add(INVALID_SIGNATURE);
        }
    }


}
