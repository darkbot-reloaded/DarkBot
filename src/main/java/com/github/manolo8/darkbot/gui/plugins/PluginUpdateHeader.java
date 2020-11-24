package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.extensions.plugins.PluginUpdater;
import com.github.manolo8.darkbot.gui.components.MainButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

//todoo i18n
public class PluginUpdateHeader extends JPanel {

    private static final MessageFormat titleFormatter = new MessageFormat("Plugin Updates: {0} Last Checked: {1}");
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, d MMM, hh:mm a", Locale.ROOT);
    private String status;

    private final PluginUpdater pluginUpdater;

    private final Title title;
    private final UpdateProgressBar progressBar;

    private final PluginDisplay pluginDisplay;

    PluginUpdateHeader(PluginUpdater pluginUpdater, PluginDisplay display) {
        super(new MigLayout("ins 0, gap 0, fill", "[grow][][]", "[][grow]"));

        this.pluginUpdater = pluginUpdater;

        this.status = pluginUpdater.hasNoUpdates() ? "You are all up to date, " : "";
        this.pluginDisplay = display;

        title = new Title();
        progressBar = new UpdateProgressBar();
        pluginUpdater.setMainProgressBar(progressBar);

        add(title);
        add(new UpdateAllButton());
        add(new CheckUpdateButton());
        add(progressBar, "dock south, spanx");
    }

    private class Title extends JLabel {
        private Title() {
            super(titleFormatter.format(new Object[]{status, dateFormatter.format(pluginUpdater.getLastChecked())}));
        }
    }

    private class CheckUpdateButton extends MainButton {

        private CheckUpdateButton() {
            super("Check for updates");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            pluginUpdater.checkUpdates();

            status = pluginUpdater.hasNoUpdates() ? "You are all up to date" : "";
            title.setText(titleFormatter.format(new Object[]{status, dateFormatter.format(pluginUpdater.getLastChecked())}));
            pluginDisplay.refreshUI();
        }
    }

    private class UpdateAllButton extends MainButton {

        private UpdateAllButton() {
            super("Update all");
            setVisible(pluginUpdater.hasAvailableUpdates());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            progressBar.setVisible(true);
            pluginUpdater.updateAll();
        }
    }

}
