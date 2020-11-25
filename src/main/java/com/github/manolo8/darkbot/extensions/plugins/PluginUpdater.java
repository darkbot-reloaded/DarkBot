package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.plugins.PluginCard;
import com.github.manolo8.darkbot.gui.plugins.PluginDisplay;
import com.github.manolo8.darkbot.gui.plugins.UpdateProgressBar;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.Time;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

//todoo i18n
public class PluginUpdater {

    private static final PluginIssue NO_DOWNLOAD = new PluginIssue(I18n.get("plugins.update_issues.no_download"),
            I18n.get("plugins.update_issues.no_download.desc"), PluginIssue.Level.INFO);
    private static final PluginIssue NO_UPDATE = new PluginIssue(I18n.get("plugins.update_issues.no_update"),
            I18n.get("plugins.update_issues.no_update.desc"), PluginIssue.Level.INFO);

    private long lastChecked = System.currentTimeMillis();

    private final PluginHandler pluginHandler;
    private PluginDisplay pluginDisplay;

    private UpdateProgressBar mainProgressBar;

    public PluginUpdater(Main main) {
        this.pluginHandler = main.pluginHandler;
    }

    public void scheduleUpdateChecker() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkUpdates(null);
            }
        }, 0, Time.DAY);
    }

    public void setPluginDisplay(PluginDisplay display) {
        pluginDisplay = display;
    }

    public void setMainProgressBar(UpdateProgressBar progressBar) {
        mainProgressBar = progressBar;
    }

    public boolean hasAvailableUpdates() {
        return pluginHandler.getUpdates()
                .anyMatch(pl -> pl.getUpdateStatus() == Plugin.UpdateStatus.AVAILABLE);
    }

    public boolean hasUpdates() {
        return pluginHandler.getUpdates().findAny().isPresent();
    }

    public long getLastChecked() {
        return lastChecked;
    }

    public void checkUpdates(Runnable doneTask) {
        new SwingWorker<Void, Void>() {
            @Override
            protected void done() {
                if (doneTask != null) doneTask.run();
                pluginDisplay.refreshUI();
            }

            @Override
            protected Void doInBackground() {
                for (Plugin plugin : pluginHandler.LOADED_PLUGINS) {
                    try {
                        checkUpdate(plugin);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                lastChecked = System.currentTimeMillis();
                return null;
            }
        }.execute();
    }

    private void checkUpdate(Plugin plugin) throws IOException {
        plugin.setUpdateDefinition(findUpdate(plugin.getDefinition()));

        PluginDefinition updateDef = plugin.getUpdateDefinition();

        IssueHandler updateIssues = plugin.getUpdateIssues();
        pluginHandler.testCompatibility(updateIssues, updateDef, true);

        if (updateIssues.getIssues().contains(NO_DOWNLOAD) || updateIssues.getIssues().contains(NO_UPDATE)) {
            plugin.setUpdateStatus(Plugin.UpdateStatus.UNKNOWN);
            return;
        }
        if (plugin.getDefinition().version.compareTo(updateDef.version) >= 0) {
            plugin.setUpdateStatus(Plugin.UpdateStatus.UP_TO_DATE);
            return;
        }

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
                    UpdateTask task = new UpdateTask(plugin, null);
                    task.execute();
                    task.get();

                    task.reloadingStatus();
                    pluginHandler.updatePlugins();

                    task.doneStatus();
                    if (task.failed) failedUpdate.append(task.plugin.getName()).append(" failed to update");

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

        private final StringBuilder failedUpdates = new StringBuilder();

        private final AtomicInteger progress = new AtomicInteger(0);
        private final List<UpdateTask> updateTasks = new ArrayList<>();

        UpdateAllTask() {
            List<Plugin> availableUpdates = pluginHandler.getUpdates()
                    .filter(pl -> pl.getUpdateStatus() == Plugin.UpdateStatus.AVAILABLE)
                    .collect(Collectors.toList());
            availableUpdates.forEach(pl -> updateTasks.add(new UpdateTask(pl, this)));
            mainProgressBar.setMaximum(availableUpdates.size() * 4 + 1);
        }

        public synchronized void publish(int progress) {
            super.publish(progress);
        }

        @Override
        protected void process(List<Integer> chunks) {
            mainProgressBar.setValue(chunks.get(chunks.size() - 1));
        }

        @Override
        protected void done() {
            mainProgressBar.setValue(mainProgressBar.getMaximum());
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
                   failedUpdates.append(task.plugin.getName()).append(" failed to update\n");
            });
            return null;
        }
    }

    private class UpdateTask extends SwingWorker<Void, Void> {

        private static final String SUFFIX = "...";

        private final Plugin plugin;
        private final PluginCard card;
        private final UpdateAllTask updateAllTask;

        private boolean failed;

        UpdateTask(Plugin plugin, UpdateAllTask updateAllTask) {
            this.plugin = plugin;
            this.card = pluginDisplay.getPluginCard(plugin);
            this.updateAllTask = updateAllTask;
        }

        private void waitUntilDone() {
            try {
                get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        private void reloadingStatus() {
            card.setUpdateProgress("Reloading plugins" + UpdateTask.SUFFIX, 75);
            publishMainTask();
        }

        private void doneStatus() {
            card.setUpdateProgress(failed ? "Update failed!" : "Successfully updated", 100);
            publishMainTask();
        }

        private void publishMainTask() {
            if (updateAllTask == null) return;
            updateAllTask.publish(updateAllTask.progress.incrementAndGet());
        }

        @Override
        protected void done() {
            plugin.setUpdateStatus(failed ? Plugin.UpdateStatus.FAILED : Plugin.UpdateStatus.UP_TO_DATE);
        }

        @Override
        protected Void doInBackground() throws Exception {
            card.disableUpdateButton();
            card.makeProgressBarVisible();

            if (plugin.getUpdateDefinition() == null)
                plugin.setUpdateDefinition(findUpdate(plugin.getDefinition()));

            try (InputStream is = plugin.getUpdateDefinition().download.openConnection().getInputStream()) {
                card.setUpdateProgress("Saving old plugin" + SUFFIX, 0);

                pluginHandler.createDirectory(PluginHandler.PLUGIN_OLD_PATH);
                Files.copy(plugin.getFile().toPath(), PluginHandler.PLUGIN_OLD_PATH.resolve(plugin.getFile().getName()), StandardCopyOption.REPLACE_EXISTING);
                card.setUpdateProgress("Downloading plugin to update folder" + SUFFIX, 25);
                publishMainTask();

                pluginHandler.createDirectory(PluginHandler.PLUGIN_UPDATE_PATH);
                Files.copy(is, PluginHandler.PLUGIN_UPDATE_PATH.resolve(plugin.getFile().getName()), StandardCopyOption.REPLACE_EXISTING);
                card.setUpdateProgress("Waiting to reload" + SUFFIX, 50);
                publishMainTask();

            } catch (IOException e) {
                System.err.println("Failed to download update");
                failed = true;
                e.printStackTrace();
            }
            return null;
        }
    }
}
