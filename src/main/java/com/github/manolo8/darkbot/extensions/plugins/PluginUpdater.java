package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.plugins.UpdateProgressBar;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.Time;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

//todoo i18n
public class PluginUpdater {

    private Date lastChecked = new Date();

    public final List<Plugin> UPDATES = new ArrayList<>();

    private final PluginHandler pluginHandler;

    private UpdateProgressBar mainProgressBar;
    private final List<PluginProgress> pluginProgresses = new ArrayList<>();

    private static class PluginProgress {
        private final Plugin plugin;
        private final UpdateProgressBar progressBar;
        private final JLabel progressLabel;

        PluginProgress(Plugin plugin, UpdateProgressBar progressBar, JLabel progressLabel) {
            this.plugin = plugin;
            this.progressBar = progressBar;
            this.progressLabel = progressLabel;
        }

        void setValue(int val) {
            progressBar.setValue(val);
        }

        void setText(String text) {
            progressLabel.setText(text);
        }
    }

    public PluginUpdater(Main main) {
        this.pluginHandler = main.pluginHandler;
    }

    public void scheduleUpdateChecker() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkUpdates();
            }
        }, 0, Time.DAY);
    }

    public void add(Plugin plugin, UpdateProgressBar progressBar, JLabel progressLabel) {
        pluginProgresses.add(new PluginProgress(plugin, progressBar, progressLabel));
    }

    public void clear() {
        pluginProgresses.clear();
    }

    public void setMainProgressBar(UpdateProgressBar progressBar) {
        mainProgressBar = progressBar;
    }

    public boolean hasAvailableUpdates() {
        return UPDATES.stream().anyMatch(pl -> pl.getUpdateStatus() == Plugin.UpdateStatus.AVAILABLE);
    }

    public boolean hasNoUpdates() {
        return UPDATES.isEmpty();
    }

    public Date getLastChecked() {
        return lastChecked;
    }

    public void checkUpdates() {
        UPDATES.clear();
        for (Plugin plugin : pluginHandler.LOADED_PLUGINS) {
            try {
                checkUpdate(plugin);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        lastChecked = new Date();
    }

    private void checkUpdate(Plugin plugin) throws IOException {
        plugin.setUpdateDefinition(findUpdate(plugin.getDefinition()));

        PluginDefinition updateDef = plugin.getUpdateDefinition();
        if (plugin.getDefinition().version.compareTo(updateDef.version) >= 0) return;

        IssueHandler updateIssues = plugin.getUpdateIssues();
        pluginHandler.testCompatibility(updateIssues, updateDef, true);

        UPDATES.add(plugin);
        plugin.setUpdateStatus(updateIssues.getIssues().isEmpty()
                ? Plugin.UpdateStatus.AVAILABLE
                : Plugin.UpdateStatus.INCOMPATIBLE);
    }

    private PluginDefinition findUpdate(PluginDefinition current) throws IOException {
        if (current.update == null) return current;
        PluginDefinition next = pluginHandler.readPluginDefinition(current.update.openStream());
        if (current.version.compareTo(next.version) >= 0) return current;
        if (next.update == null || current.update.equals(next.update)) return next;
        return findUpdate(next);
    }

    public void update(Plugin plugin) {
        new SwingWorker<Void, Void>() {
            private final StringBuilder failedUpdate = new StringBuilder();

            @Override
            protected void done() {
                if (failedUpdate.length() <= 0) return;
                Popups.showMessageAsync("Update failed", failedUpdate, JOptionPane.ERROR_MESSAGE);
            }

            @Override
            protected Void doInBackground() {
                try {
                    UpdateTask task = new UpdateTask(plugin, null, true);
                    task.execute();
                    task.get();

                    task.reloadingStatus();
                    pluginHandler.updatePlugins();

                    task.doneStatus();
                    if (task.failed) failedUpdate.append(task.plugin.plugin.getName()).append(" failed to update");

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public void updateAll() {
        new UpdateAllTask().execute();
    }

    private class UpdateAllTask extends SwingWorker<Void, Integer> {

        private final List<Plugin> availableUpdates = UPDATES.stream()
                .filter(pl -> pl.getUpdateStatus() == Plugin.UpdateStatus.AVAILABLE)
                .collect(Collectors.toList());
        private final StringBuilder failedUpdates = new StringBuilder();

        private final AtomicInteger progress = new AtomicInteger(0);
        private final int increment = (int) (25.0D / availableUpdates.size());
        private final List<UpdateTask> updateTasks = new ArrayList<>();
        {
            availableUpdates.forEach(pl -> updateTasks.add(new UpdateTask(pl, this, false)));
        }

        public synchronized void publish(int progress) {
            super.publish(progress);
        }

        @Override
        protected synchronized void process(List<Integer> chunks) {
            mainProgressBar.setValue(chunks.get(chunks.size() - 1));
        }

        @Override
        protected void done() {
            pluginProgresses.clear();
            mainProgressBar.setValue(100);
            if (failedUpdates.length() > 0)
                Popups.showMessageAsync("Update failed", failedUpdates, JOptionPane.ERROR_MESSAGE);
        }

        @Override
        protected Void doInBackground() {
            updateTasks.forEach(SwingWorker::execute);
            updateTasks.forEach(UpdateTask::waitUntilDone);

            updateTasks.forEach(UpdateTask::reloadingStatus);
            pluginHandler.updatePlugins();

            updateTasks.forEach(UpdateTask::doneStatus);
            updateTasks.forEach(task -> {
                if (task.failed)
                   failedUpdates.append(task.plugin.plugin.getName()).append(" failed to update\n");
            });
            return null;
        }
    }

    private class UpdateTask extends SwingWorker<Void, Void> {

        private static final String SUFFIX = "...";

        private final PluginProgress plugin;
        private final UpdateAllTask updateAllTask;

        private final boolean remove;
        private boolean failed;

        UpdateTask(Plugin plugin, UpdateAllTask updateAllTask, boolean remove) {
            this.plugin = pluginProgresses.stream().filter(p -> p.plugin.equals(plugin)).findFirst().orElse(null);
            this.updateAllTask = updateAllTask;
            this.remove = remove;
        }

        private void waitUntilDone() {
            try {
                get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        private void reloadingStatus() {
            plugin.setText("Reloading plugins" + UpdateTask.SUFFIX);
            plugin.setValue(75);
            publishMainTask();
        }

        private void doneStatus() {
            plugin.setText(failed ? "Update failed!" : "Successfully updated");
            plugin.setValue(100);
            publishMainTask();
            if (!failed) UPDATES.remove(plugin.plugin);
        }

        private void publishMainTask() {
            if (updateAllTask == null) return;
            updateAllTask.publish(updateAllTask.progress.addAndGet(updateAllTask.increment));
        }

        @Override
        protected void done() {
            if (!failed) plugin.plugin.setUpdateStatus(Plugin.UpdateStatus.UP_TO_DATE);
            if (remove) pluginProgresses.remove(plugin);
        }

        @Override
        protected Void doInBackground() throws Exception {
            Plugin pl = plugin.plugin;
            if (pl.getUpdateDefinition() == null)
                pl.setUpdateDefinition(findUpdate(pl.getDefinition()));
            try (InputStream is = pl.getUpdateDefinition().download.openConnection().getInputStream()) {
                plugin.setValue(0);
                plugin.progressBar.setVisible(true);

                plugin.setText("Saving old plugin" + SUFFIX);
                pluginHandler.createDirectory(PluginHandler.PLUGIN_OLD_PATH);
                Files.copy(pl.getFile().toPath(), PluginHandler.PLUGIN_OLD_PATH.resolve(pl.getFile().getName()), StandardCopyOption.REPLACE_EXISTING);
                plugin.setValue(25);
                publishMainTask();

                plugin.setText("Downloading plugin to update folder" + SUFFIX);
                pluginHandler.createDirectory(PluginHandler.PLUGIN_UPDATE_PATH);
                Files.copy(is, PluginHandler.PLUGIN_UPDATE_PATH.resolve(pl.getFile().getName()), StandardCopyOption.REPLACE_EXISTING);
                plugin.setValue(50);
                publishMainTask();

                plugin.setText("Waiting to reload" + SUFFIX);

            } catch (IOException e) {
                System.err.println("Failed to download update");
                failed = true;
                e.printStackTrace();
            }
            return null;
        }
    }
}
