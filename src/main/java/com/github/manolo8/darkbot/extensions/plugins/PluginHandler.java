package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.utils.AuthAPI;
import com.github.manolo8.darkbot.utils.FileUtils;
import com.github.manolo8.darkbot.utils.I18n;
import com.google.gson.Gson;

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

public class PluginHandler {
    private static final Gson GSON = new Gson();

    public static final PluginIssue NO_DOWNLOAD = new PluginIssue(I18n.get("plugins.update_issues.no_download"),
            I18n.get("plugins.update_issues.no_download.desc"), PluginIssue.Level.INFO);
    public static final PluginIssue NO_UPDATE = new PluginIssue(I18n.get("plugins.update_issues.no_update"),
            I18n.get("plugins.update_issues.no_update.desc"), PluginIssue.Level.INFO);

    public static final File PLUGIN_FOLDER = new File("plugins"),
            PLUGIN_UPDATE_FOLDER = new File("plugins/updates"),
            PLUGIN_OLD_FOLDER    = new File("plugins/old");
    public static final Path PLUGIN_PATH = PLUGIN_FOLDER.toPath(),
            PLUGIN_UPDATE_PATH = PLUGIN_UPDATE_FOLDER.toPath(),
            PLUGIN_OLD_PATH    = PLUGIN_OLD_FOLDER.toPath();

    public URLClassLoader PLUGIN_CLASS_LOADER;
    public final List<Plugin> LOADED_PLUGINS = new ArrayList<>();
    public final List<Plugin> FAILED_PLUGINS = new ArrayList<>();
    public final List<PluginLoadingException> LOADING_EXCEPTIONS = new ArrayList<>();

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
            List<Plugin> plugins = new ArrayList<>(LOADED_PLUGINS);

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

            FileUtils.createDirectory(PLUGIN_PATH);
            for (File plugin : getJars(PLUGIN_UPDATE_FOLDER)) {
                Path plPath = plugin.toPath();
                try {
                    Files.move(plPath, PLUGIN_PATH.resolve(plPath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    LOADING_EXCEPTIONS.add(new PluginLoadingException("Failed to update plugin: " + plPath.getFileName(), e));
                    e.printStackTrace();
                }
            }
            try {
                loadPlugins(getJars(PLUGIN_FOLDER), plugins);
            } catch (Exception e) {
                LOADING_EXCEPTIONS.add(new PluginLoadingException("Failed to load plugins", e));
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

    private void loadPlugins(File[] pluginFiles, List<Plugin> plugins) {
        for (File pluginFile : pluginFiles) {
            Plugin pl = null;
            try {
                pl = new Plugin(pluginFile, pluginFile.toURI().toURL());
                loadPlugin(pl);

                // need to copy over previous update status and update issues or else they will be lost
                if (plugins.contains(pl)) {
                    Plugin plugin = plugins.get(plugins.indexOf(pl));
                    pl.setUpdateStatus(plugin.getUpdateStatus());
                    plugin.getUpdateIssues().getIssues().forEach(pl::add);
                    pl.setUpdateDefinition(plugin.getUpdateDefinition());
                }

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
            plugin.getIssues().addFailure(I18n.get("plugins.issues.loaded_twice"),
                    I18n.get("plugins.issues.loaded_twice.desc"));
    }

    private void testCompatibility(Plugin plugin) {
        testCompatibility(plugin.getIssues(), plugin.getDefinition(), false);
    }

    //todoo i18n
    void testCompatibility(IssueHandler issues, PluginDefinition pd, boolean isUpdate) {
        if (isUpdate && pd.download == null)
            issues.getIssues().add(NO_DOWNLOAD);
        if (isUpdate && pd.update == null)
            issues.getIssues().add(NO_UPDATE);

        if (pd.minVersion.compareTo(pd.supportedVersion) > 0)
            issues.addFailure(I18n.get(isUpdate ? "plugins.update_issues.invalid_json" : "plugins.issues.invalid_json"),
                    I18n.get(isUpdate ? "plugins.update_issues.invalid_json.desc" : "plugins.issues.invalid_json.desc",
                            pd.minVersion, pd.supportedVersion));

        String supportedRange = "DarkBot v" + (pd.minVersion.compareTo(pd.supportedVersion) == 0 ?
                pd.minVersion : pd.minVersion + "-v" + pd.supportedVersion);

        if (Main.VERSION.compareTo(pd.minVersion) < 0)
            issues.addFailure(I18n.get(isUpdate ? "plugins.update_issues.bot_update" : "plugins.issues.bot_update"),
                    I18n.get(isUpdate ? "plugins.update_issues.bot_update.desc" : "plugins.issues.bot_update.desc",
                            supportedRange, Main.VERSION));

        if (!isUpdate && Main.VERSION.compareTo(pd.supportedVersion) > 0)
            issues.addInfo(I18n.get("plugins.issues.plugin_update"),
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
