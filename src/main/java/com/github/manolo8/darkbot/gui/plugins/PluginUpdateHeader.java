package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginUpdater;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.utils.I18n;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.stream.Collectors;

public class PluginUpdateHeader extends JPanel {

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, d MMM, hh:mm a", Locale.ROOT);

    private final PluginUpdater pluginUpdater;
    private final PluginHandler pluginHandler;

    private final JLabel title;
    private final JProgressBar progressBar;
    private final UpdateAllButton updateAllButton;
    private final CheckUpdateButton checkUpdateButton;

    PluginUpdateHeader(Main main) {
        super(new MigLayout("ins 0, gap 0, fill", "[grow][][]", "[][grow]"));

        this.pluginHandler = main.pluginHandler;
        this.pluginUpdater = main.pluginUpdater;

        add(this.title = new JLabel());
        add(this.updateAllButton = new UpdateAllButton());
        add(this.checkUpdateButton = new CheckUpdateButton());
        add(this.progressBar = new JProgressBar(), "dock south, spanx");

        progressBar.setBorderPainted(false);

        refreshUI();
    }

    void refreshUI() {
        updateAllButton.refresh();
        progressBar.setVisible(false);

        checkUpdateButton.setEnabled(true);
        String status = !pluginUpdater.hasAnyUpdates()
                ? I18n.get("plugins.config_button.up_to_date")
                : I18n.get("plugins.config_button.out_of_date", getOutOfDateCount());
        title.setText(I18n.get("plugins.config_button.update_header",
                status, dateFormatter.format(pluginUpdater.getLastChecked())));
    }

    private long getOutOfDateCount() {
        return pluginHandler.LOADED_PLUGINS.stream()
                .filter(pl -> pl.getUpdateStatus() == Plugin.UpdateStatus.AVAILABLE ||
                        pl.getUpdateStatus() == Plugin.UpdateStatus.INCOMPATIBLE ||
                        pl.getUpdateStatus() == Plugin.UpdateStatus.FAILED).count();
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    private class CheckUpdateButton extends MainButton {

        private CheckUpdateButton() {
            super(I18n.get("plugins.config_button.check_updates"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            pluginUpdater.checkUpdates();
        }
    }

    private class UpdateAllButton extends MainButton {

        private UpdateAllButton() {
            super(I18n.get("plugins.config_button.update_all"));
            refresh();
        }

        private void refresh() {
            setEnabled(true);

            String toolTipText = pluginHandler.getAvailableUpdates()
                    .map(pl -> I18n.get("plugins.config_button.update_all.desc",
                            pl.getName(),
                            pl.getDefinition().version,
                            pl.getUpdateDefinition().version))
                    .collect(Collectors.joining("\n"));

            if (!toolTipText.isEmpty()) {
                setToolTipText(toolTipText);
                setVisible(true);
            } else  {
                setToolTipText(null);
                setVisible(false);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            progressBar.setVisible(true);
            pluginUpdater.updateAll();
        }
    }

}
