package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

//todoo i18n
public class UpdateTask extends SwingWorker<Void, String> {

    private static final String SUFFIX = "...";

    volatile boolean canReload;
    private boolean failed, reload, isReloading = true;

    private final PluginHandler pluginHandler;
    private final Plugin plugin;
    private final UpdateProgressBar progressBar;
    private final JLabel progressLabel;

    UpdateTask(Main main, Plugin plugin, UpdateProgressBar progressBar, JLabel progressLabel) {
        this.pluginHandler = main.pluginHandler;
        this.plugin = plugin;
        this.progressBar = progressBar;
        this.progressLabel = progressLabel;
    }

    void makeReloadable() {
        reload = true;
    }

    void finishedReloading() {
        isReloading = false;
    }

    @Override
    protected void process(List<String> chunks) {
        progressLabel.setText(chunks.get(chunks.size() - 1));
    }

    @Override
    protected void done() {
        if (failed) {
            System.err.println("\"" + plugin.getName() + "\" plugin update failed");
            progressLabel.setText("Update failed");
            progressBar.setForeground(Color.RED);
        } else {
            System.out.println("Successfully updated \"" + plugin.getName() + "\" plugin");
            progressLabel.setText("Update completed");
        }
        progressBar.setValue(100);
        firePropertyChange("progress", 75, 100);
    }

    @Override
    protected Void doInBackground() throws Exception {
        try (InputStream is = plugin.getUpdateDefinition().download.openConnection().getInputStream()) {
            firePropertyChange("progress", 0, 0);
            progressBar.setValue(0);
            progressBar.setVisible(true);
            progressLabel.setText("Saving old plugin" + SUFFIX);
            if (Files.notExists(PluginHandler.PLUGIN_OLD_PATH)) Files.createDirectory(PluginHandler.PLUGIN_OLD_PATH);
            Files.copy(plugin.getFile().toPath(), PluginHandler.PLUGIN_OLD_PATH.resolve(plugin.getFile().getName()), StandardCopyOption.REPLACE_EXISTING);
            progressBar.setValue(25);
            firePropertyChange("progress", 0, 25);

            progressLabel.setText("Downloading plugin to update folder" + SUFFIX);
            if (Files.notExists(PluginHandler.PLUGIN_UPDATE_PATH)) Files.createDirectory(PluginHandler.PLUGIN_UPDATE_PATH);
            Files.copy(is, PluginHandler.PLUGIN_UPDATE_PATH.resolve(plugin.getFile().getName()), StandardCopyOption.REPLACE_EXISTING);
            progressBar.setValue(50);
            firePropertyChange("progress", 25, 50);

            canReload = true;
            progressLabel.setText("Waiting to reload" + SUFFIX);
            while (!reload) Thread.sleep(100);

            progressLabel.setText("Reloading plugins" + SUFFIX);
            progressBar.setValue(75);
            firePropertyChange("progress", 50, 75);
            while (isReloading) Thread.sleep(100);

            pluginHandler.AVAILABLE_UPDATES.remove(plugin);
        } catch (IOException e) {
            System.err.println("Failed to download update");
            failed = true;
            e.printStackTrace();
        }
        return null;
    }
}
