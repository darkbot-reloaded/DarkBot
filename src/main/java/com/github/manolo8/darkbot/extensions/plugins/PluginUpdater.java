package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.gui.plugins.PluginCard;
import com.github.manolo8.darkbot.gui.plugins.PluginDisplay;
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

public class PluginUpdater {

    private static final String DOWNLOAD_FAILED = "plugins.update_issues.download_failed";

    public final List<PluginException> UPDATING_EXCEPTIONS = new ArrayList<>();
    private volatile long lastChecked = System.currentTimeMillis();

    private final PluginHandler pluginHandler;
    private PluginDisplay pluginDisplay;

    private JProgressBar mainProgressBar;

    public PluginUpdater(PluginHandler pluginHandler) {
        this.pluginHandler = pluginHandler;
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
                        pl.getUpdateStatus() == Plugin.UpdateStatus.INCOMPATIBLE ||
                        pl.getUpdateStatus() == Plugin.UpdateStatus.FAILED);
    }

    public long getLastChecked() {
        return lastChecked;
    }

    public void checkUpdates() {
        UPDATING_EXCEPTIONS.clear();
        new SwingWorker<Void, Void>() {
            @Override
            protected void done() {
                pluginDisplay.refreshUI();
            }

            @Override
            protected Void doInBackground() {
                for (Plugin plugin : pluginHandler.LOADED_PLUGINS) {
                    plugin.getUpdateIssues().getIssues()
                            .removeIf(pl -> pl.getMessageKey().equals(DOWNLOAD_FAILED));
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

        if (updateIssues.getIssues().contains(PluginHandler.UPDATE_NOT_POSSIBLE)) {
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

        private int progress = 0;
        private final List<UpdateTask> updateTasks;

        UpdateAllTask() {
            updateTasks = pluginHandler.getAvailableUpdates()
                    .map(pl -> new UpdateTask(pl, this))
                    .collect(Collectors.toList());
            mainProgressBar.setMaximum(updateTasks.size() * 4 + 1);
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
        }

        @Override
        protected Void doInBackground() {
            updateTasks.forEach(SwingWorker::execute);
            updateTasks.forEach(UpdateTask::waitUntilDone);

            pluginHandler.updatePlugins();

            return null;
        }
    }

    private class UpdateTask extends SwingWorker<Void, PluginCard.UpdateStatus> {

        private final Plugin plugin;
        private final UpdateAllTask updateAllTask;
        private final PluginCard card;

        private final boolean isUpdatingAll;
        private boolean failed;

        UpdateTask(Plugin plugin) {
            this(plugin, null);
        }

        UpdateTask(Plugin plugin, UpdateAllTask updateAllTask) {
            this.plugin = plugin;
            this.updateAllTask = updateAllTask;
            this.card = pluginDisplay.getPluginCard(plugin);
            this.isUpdatingAll = updateAllTask != null;
            card.setProgressBarMaximum(isUpdatingAll ? 3 : 4);
        }

        private void waitUntilDone() {
            try {
                get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        private void publish(PluginCard.UpdateStatus status) {
            super.publish(status);
            if (isUpdatingAll) updateAllTask.tickUpdate();
        }

        @Override
        protected void process(List<PluginCard.UpdateStatus> chunks) {
            card.setUpdateProgress(chunks.get(chunks.size() - 1));
        }

        @Override
        protected void done() {
            card.setUpdateProgress(failed ? PluginCard.UpdateStatus.FAILED :
                    isUpdatingAll ? PluginCard.UpdateStatus.INDIVIDUALLY_DONE : PluginCard.UpdateStatus.DONE);
            if (!isUpdatingAll && failed) pluginDisplay.refreshUI(); // Ensure the exception card is shown
        }

        @Override
        protected Void doInBackground() throws Exception {
            publish(PluginCard.UpdateStatus.STARTING);

            if (plugin.getUpdateDefinition() == null)
                plugin.setUpdateDefinition(findUpdate(plugin.getDefinition()));

            try (InputStream is = plugin.getUpdateDefinition().download.openConnection().getInputStream()) {
                publish(PluginCard.UpdateStatus.SAVING_OLD);

                FileUtils.ensureDirectoryExists(PluginHandler.PLUGIN_OLD_PATH);
                Files.copy(plugin.getFile().toPath(), PluginHandler.PLUGIN_OLD_PATH.resolve(plugin.getFile().getName()), StandardCopyOption.REPLACE_EXISTING);
                publish(PluginCard.UpdateStatus.DOWNLOADING);

                FileUtils.ensureDirectoryExists(PluginHandler.PLUGIN_UPDATE_PATH);
                Files.copy(is, PluginHandler.PLUGIN_UPDATE_PATH.resolve(plugin.getFile().getName()), StandardCopyOption.REPLACE_EXISTING);

                plugin.setUpdateStatus(Plugin.UpdateStatus.UP_TO_DATE);
                if (isUpdatingAll) return null;

                publish(PluginCard.UpdateStatus.RELOADING);
                pluginHandler.updatePlugins();

            } catch (IOException e) {
                System.err.println("Failed to download update");
                failed = true;
                UPDATING_EXCEPTIONS.add(new PluginException("Failed to download update", e, plugin));
                plugin.getUpdateIssues().addFailure(DOWNLOAD_FAILED, IssueHandler.createDescription(e));
                plugin.setUpdateStatus(Plugin.UpdateStatus.FAILED);
                e.printStackTrace();
            }
            return null;
        }
    }

}
