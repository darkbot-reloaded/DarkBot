package com.github.manolo8.darkbot.extensions;

import com.github.manolo8.darkbot.config.types.suppliers.ModuleSupplier;
import com.github.manolo8.darkbot.core.itf.CustomModule;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.modules.CollectorModule;
import com.github.manolo8.darkbot.modules.EventModule;
import com.github.manolo8.darkbot.modules.LootModule;
import com.github.manolo8.darkbot.modules.LootNCollectorModule;
import com.google.gson.Gson;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ModuleHandler {

    private static final Gson GSON = new Gson();

    private URLClassLoader PLUGIN_CLASS_LOADER;
    private Map<String, Class<? extends Module>> moduleRegistry = new LinkedHashMap<>();

    private void registerCoreModules() {
        moduleRegistry.put("Collector", CollectorModule.class);
        moduleRegistry.put("Npc Killer", LootModule.class);
        moduleRegistry.put("Kill & Collect", LootNCollectorModule.class);
        moduleRegistry.put("Experiment zones", EventModule.class);
    }

    public void reloadModules() {
        moduleRegistry.clear();
        registerCoreModules();

        try {
            loadPlugins(new File("plugins").listFiles());
        } catch (Exception e) {
            System.err.println("Failed to load plugins");
            e.printStackTrace();
        }

        ModuleSupplier.MODULES = new ArrayList<>(moduleRegistry.keySet());
    }

    private void loadPlugins(File[] plugins) throws IOException {
        if (PLUGIN_CLASS_LOADER != null) {
            PLUGIN_CLASS_LOADER.close();
        }
        if (plugins == null) return;
        List<URL> urls = new ArrayList<>();
        List<ModuleDefinition> definitions = new ArrayList<>();
        for (File plugin : plugins) {
            try {
                urls.add(loadPlugin(plugin, definitions));
            } catch (Exception e) {
                System.err.println("Could not load plugin: " + plugin.getName());
                e.printStackTrace();
            }
        }
        PLUGIN_CLASS_LOADER = new URLClassLoader(urls.toArray(new URL[0]));
        for (ModuleDefinition moduleDefinition : definitions) {
            try {
                Class<?> clazz = PLUGIN_CLASS_LOADER.loadClass(moduleDefinition.module);
                //noinspection unchecked
                moduleRegistry.put(moduleDefinition.name, (Class<? extends CustomModule>) clazz);
            } catch (Exception e) {
                System.err.println("Could not load module: " + moduleDefinition.name);
                e.printStackTrace();
            }
        }
    }

    private URL loadPlugin(File plFile, List<ModuleDefinition> modules) throws IOException {
        JarFile jar = new JarFile(plFile);
        ZipEntry plJson = jar.getEntry("plugin.json");
        if (plJson == null) {
            throw new IllegalArgumentException("missing plugin.json");
        }
        PluginDefinition plugin = GSON.fromJson(new InputStreamReader(jar.getInputStream(plJson), StandardCharsets.UTF_8), PluginDefinition.class);

        URL url = plFile.toURI().toURL();
        modules.addAll(Arrays.asList(plugin.modules));

        return url;
    }

    public Module getModule(String id) {
        Class<? extends Module> moduleClass = moduleRegistry.get(id);
        if (moduleClass == null) {
            Popups.showMessageAsync("Error", "Failed to find module " + id + ", using default", JOptionPane.ERROR_MESSAGE);
            return new LootNCollectorModule();
        }
        try {
            return moduleClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            Popups.showMessageAsync("Error", "Failed to load module " + id + ", using default", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return new LootNCollectorModule();
    }

}
