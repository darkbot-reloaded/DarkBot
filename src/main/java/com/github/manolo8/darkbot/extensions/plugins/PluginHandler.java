package com.github.manolo8.darkbot.extensions.plugins;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class PluginHandler {
    private static final Gson GSON = new Gson();

    public URLClassLoader PLUGIN_CLASS_LOADER;
    public List<Plugin> LOADED_PLUGINS = new ArrayList<>();

    private static List<PluginListener> LISTENERS = new ArrayList<>();

    public void addListener(PluginListener listener) {
        LISTENERS.add(listener);
    }

    public void updatePlugins() {
        LISTENERS.forEach(PluginListener::beforeLoad);
        try {
            loadPlugins(new File("plugins").listFiles());
        } catch (Exception e) {
            System.err.println("Failed to load plugins");
            e.printStackTrace();
        }
        LISTENERS.forEach(PluginListener::afterLoad);
    }

    private void loadPlugins(File[] pluginFiles) throws IOException {
        if (PLUGIN_CLASS_LOADER != null) PLUGIN_CLASS_LOADER.close();
        if (pluginFiles == null) return;
        LOADED_PLUGINS.clear();
        for (File plugin : pluginFiles) {
            try {
                LOADED_PLUGINS.add(loadPlugin(plugin));
            } catch (Exception e) {
                System.err.println("Could not load plugin: " + plugin.getName());
                e.printStackTrace();
            }
        }
        PLUGIN_CLASS_LOADER = new URLClassLoader(LOADED_PLUGINS.stream().map(Plugin::getJar).toArray(URL[]::new));
    }

    private Plugin loadPlugin(File plFile) throws IOException {
        JarFile jar = new JarFile(plFile);
        ZipEntry plJson = jar.getEntry("plugin.json");
        if (plJson == null) {
            throw new IllegalArgumentException("missing plugin.json");
        }
        PluginDefinition plugin = GSON.fromJson(new InputStreamReader(jar.getInputStream(plJson), StandardCharsets.UTF_8), PluginDefinition.class);

        return new Plugin(plugin, plFile.toURI().toURL());
    }

}
