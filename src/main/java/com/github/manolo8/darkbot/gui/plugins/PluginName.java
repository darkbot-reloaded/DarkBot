package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.extensions.plugins.PluginDefinition;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginIssue;
import com.github.manolo8.darkbot.gui.components.MainButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static com.github.manolo8.darkbot.extensions.plugins.PluginHandler.PLUGIN_OLD_PATH;
import static com.github.manolo8.darkbot.extensions.plugins.PluginHandler.PLUGIN_UPDATE_PATH;

class PluginName extends JPanel {

    private final PluginHandler pluginHandler;
    private final Plugin plugin;

    private final UpdateProgressBar progressBar;
    private final JLabel progressLabel;
    private final UpdateButton updateButton;

    volatile boolean canReload;
    UpdateTask task;

    PluginName(Main main, Plugin plugin, UpdateProgressBar progressBar, JLabel progressLabel) {
        super(new MigLayout("", "[]5px[]5px[]5px[grow][]"));

        this.pluginHandler = main.pluginHandler;
        this.plugin = plugin;
        this.progressBar = progressBar;
        this.progressLabel = progressLabel;

        updateButton = new UpdateButton();

        PluginDefinition definition = plugin.getDefinition();
        JLabel name = new JLabel(definition.name);
        Font baseFont = name.getFont();
        name.setFont(baseFont.deriveFont(baseFont.getStyle() | Font.BOLD));

        JLabel version = new JLabel("v" + definition.version);
        version.setFont(baseFont.deriveFont(baseFont.getStyle(), baseFont.getSize() * 0.8f));

        JLabel by = new JLabel("by");

        JLabel author = new JLabel(definition.author);
        author.setFont(baseFont.deriveFont(baseFont.getStyle() | Font.ITALIC));

        add(name);
        add(version);
        add(by);
        add(author);
        add(pluginHandler.INCOMPATIBLE_UPDATES.containsKey(plugin)
                ? new IssueList(pluginHandler.INCOMPATIBLE_UPDATES.keySet().stream()
                    .filter(pl -> pl.equals(plugin))
                    .findFirst().get().getIssues(), i -> i.getLevel() == PluginIssue.Level.ERROR,false)
                : updateButton);
        setOpaque(false);
    }

    //todoo i18n
    private class UpdateButton extends MainButton {

        private UpdateButton() {
            super("Update");
            setVisible(pluginHandler.AVAILABLE_UPDATES.containsKey(plugin));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            PluginName.this.update();
            while (!canReload);
            task.makeReloadable();
            pluginHandler.updatePlugins();
            task.finishedReloading();
        }
    }

    void update() {
        task = new UpdateTask();
        task.execute();
        updateButton.setEnabled(false);
    }

    //todoo i18n
    class UpdateTask extends SwingWorker<Void, String> {

        private static final String SUFFIX = "...";

        private boolean failed, reload, isReloading = true;

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
                System.err.println("Plugin update failed");
                progressLabel.setText("Update failed");
                progressBar.setForeground(Color.RED);
            } else {
                System.out.println("Successfully updated plugin");
                progressLabel.setText("Update completed");
            }
            progressBar.setValue(100);
            firePropertyChange("progress", 75, 100);
        }

        @Override
        protected Void doInBackground() throws Exception {
            try (InputStream is = pluginHandler.AVAILABLE_UPDATES.get(plugin).download.openConnection().getInputStream()) {
                firePropertyChange("progress", 0, 0);
                progressBar.setValue(0);
                progressBar.setVisible(true);
                progressLabel.setText("Saving old plugin" + SUFFIX);
                if (Files.notExists(PLUGIN_OLD_PATH)) Files.createDirectory(PLUGIN_OLD_PATH);
                Files.copy(plugin.getFile().toPath(), PLUGIN_OLD_PATH.resolve(plugin.getFile().getName()), StandardCopyOption.REPLACE_EXISTING);
                progressBar.setValue(25);
                firePropertyChange("progress", 0, 25);

                progressLabel.setText("Downloading plugin to update folder" + SUFFIX);
                if (Files.notExists(PLUGIN_UPDATE_PATH)) Files.createDirectory(PLUGIN_UPDATE_PATH);
                Files.copy(is, PLUGIN_UPDATE_PATH.resolve(plugin.getFile().getName()), StandardCopyOption.REPLACE_EXISTING);
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

}
