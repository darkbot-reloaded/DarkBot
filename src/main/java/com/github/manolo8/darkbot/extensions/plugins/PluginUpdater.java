package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.plugins.PluginCard;
import com.github.manolo8.darkbot.gui.plugins.PluginDisplay;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.FileUtils;
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
import java.util.stream.Collectors;

//todoo i18n
public class PluginUpdater {

    private long lastChecked = System.currentTimeMillis();

    private final PluginHandler pluginHandler;
    private PluginDisplay pluginDisplay;

    private JProgressBar mainProgressBar;

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

    public void setup(PluginDisplay display) {
        pluginDisplay = display;
        mainProgressBar = display.getMainProgressBar();
    }

    public boolean hasAnyUpdates() {
        return pluginHandler.LOADED_PLUGINS.stream()
                .anyMatch(pl -> pl.getUpdateStatus() == Plugin.UpdateStatus.AVAILABLE ||
                        pl.getUpdateStatus() == Plugin.UpdateStatus.INCOMPATIBLE);
    }

    public long getLastChecked() {
        return lastChecked;
    }

    public void checkUpdates() {
        new SwingWorker<Void, Void>() {
            @Override
            protected void done() {
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

        if (updateIssues.getIssues().contains(PluginHandler.NO_DOWNLOAD) ||
                updateIssues.getIssues().contains(PluginHandler.NO_UPDATE)) {
            plugin.setUpdateStatus(Plugin.UpdateStatus.UNKNOWN);
            return;
        }
        if (plugin.getDefinition().version.compareTo(updateDef.version) >= 0) {
            plugin.setUpdateStatus(Plugin.UpdateStatus.UP_TO_DATE);
            return;
        }

        plugin.setUpdateStatus(updateIssues.canLoad()
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
        new UpdateTask(plugin).execute();
    }

    public void updateAll() {
        new UpdateAllTask().execute();
    }

    private class UpdateAllTask extends SwingWorker<Void, Integer> {

        private final StringBuilder failedUpdates = new StringBuilder();

        private int progress = 0;
        private final List<UpdateTask> updateTasks = new ArrayList<>();

        UpdateAllTask() {
            List<Plugin> availableUpdates = pluginHandler.getAvailableUpdates().collect(Collectors.toList());
            availableUpdates.forEach(pl -> updateTasks.add(new UpdateTask(pl, this)));
            mainProgressBar.setMaximum(availableUpdates.size() * 5 + 1);
        }

        public synchronized void tickUpdate() {
            super.publish(progress += 1);
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

            pluginHandler.updatePlugins();

            updateTasks.forEach(task -> {
                if (task.failed)
                   failedUpdates.append(task.plugin.getName()).append(" failed to update\n");
            });
            return null;
        }
    }

    private class UpdateTask extends SwingWorker<Void, Void> {

        private final StringBuilder failedUpdate = new StringBuilder();

        private final Plugin plugin;
        private final UpdateAllTask updateAllTask;
        private final ProgressManager progressManager;

        private boolean failed;

        UpdateTask(Plugin plugin) {
            this(plugin, null);
        }

        UpdateTask(Plugin plugin, UpdateAllTask updateAllTask) {
            this.plugin = plugin;
            this.updateAllTask = updateAllTask;
            PluginCard card = pluginDisplay.getPluginCard(plugin);
            this.progressManager = new ProgressManager(card, this);
        }

        private void waitUntilDone() {
            try {
                get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void done() {
            plugin.setUpdateStatus(failed ? Plugin.UpdateStatus.FAILED : Plugin.UpdateStatus.UP_TO_DATE);
            if (updateAllTask != null || failedUpdate.length() <= 0) return;

            Popups.showMessageAsync("Update failed", failedUpdate, JOptionPane.ERROR_MESSAGE);
        }

        @Override
        protected Void doInBackground() throws Exception {
            progressManager.setUpdateProgress(ProgressManager.State.STARTING);

            if (plugin.getUpdateDefinition() == null)
                plugin.setUpdateDefinition(findUpdate(plugin.getDefinition()));

            try (InputStream is = plugin.getUpdateDefinition().download.openConnection().getInputStream()) {
                progressManager.setUpdateProgress(ProgressManager.State.SAVING_OLD);

                FileUtils.createDirectory(PluginHandler.PLUGIN_OLD_PATH);
                Files.copy(plugin.getFile().toPath(), PluginHandler.PLUGIN_OLD_PATH.resolve(plugin.getFile().getName()), StandardCopyOption.REPLACE_EXISTING);
                progressManager.setUpdateProgress(ProgressManager.State.DOWNLOADING);

                FileUtils.createDirectory(PluginHandler.PLUGIN_UPDATE_PATH);
                Files.copy(is, PluginHandler.PLUGIN_UPDATE_PATH.resolve(plugin.getFile().getName()), StandardCopyOption.REPLACE_EXISTING);
                progressManager.setUpdateProgress(ProgressManager.State.WAITING_RELOAD);

                if (updateAllTask != null) return null;

                progressManager.setUpdateProgress(ProgressManager.State.RELOADING);
                pluginHandler.updatePlugins();
                progressManager.setUpdateProgress(ProgressManager.State.DONE);

            } catch (IOException e) {
                System.err.println("Failed to download update");
                failed = true;
                failedUpdate.append(plugin.getName()).append(" failed to update");
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class ProgressManager {
        private enum State {
            STARTING, SAVING_OLD, DOWNLOADING, WAITING_RELOAD, RELOADING, DONE
        }

        private static final String SUFFIX = "...";

        private final PluginCard card;
        private final UpdateTask task;
        private final int maxProgress;
        private final boolean isUpdatingAll;

        ProgressManager(PluginCard card, UpdateTask task) {
            this.card = card;
            this.task = task;
            this.isUpdatingAll = task.updateAllTask != null;
            this.maxProgress = isUpdatingAll ? 3 : 5;
            card.setProgressBarMaximum(maxProgress);
        }

        //todoo i18n
        private void setUpdateProgress(State state) {
            if (isUpdatingAll) task.updateAllTask.tickUpdate();
            switch (state) {
                case STARTING:
                    card.startPluginUpdate();
                    card.setUpdateProgress("", 0);
                    break;
                case SAVING_OLD:
                    card.setUpdateProgress("Saving old plugin" + SUFFIX, 1);
                    break;
                case DOWNLOADING:
                    card.setUpdateProgress("Downloading plugin to update folder" + SUFFIX, 2);
                    break;
                case WAITING_RELOAD:
                    if (isUpdatingAll) setUpdateProgress(State.DONE);
                    else card.setUpdateProgress("Waiting to reload" + SUFFIX, 3);
                    break;
                case RELOADING:
                    card.setUpdateProgress("Reloading plugins" + SUFFIX, 4);
                    break;
                case DONE:
                    card.setUpdateProgress(task.failed ? "Update failed!" : "Successfully updated", maxProgress);
                    break;
            }
        }
    }
}
