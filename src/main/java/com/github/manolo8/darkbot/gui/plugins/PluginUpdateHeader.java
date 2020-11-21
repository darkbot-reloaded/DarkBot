package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.gui.components.MainButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

//todoo i18n
public class PluginUpdateHeader extends JPanel {

    private static final MessageFormat titleFormatter = new MessageFormat("Plugin Updates: {0} Last Checked: {1}");
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, d MMM, hh:mm a", Locale.ROOT);
    private Date lastChecked = new Date();
    private String status;

    private final PluginHandler pluginHandler;

    private final Title title;
    private final UpdateAllButton updateAllButton;
    private volatile UpdateProgressBar progressBar;

    private final List<PluginName> names;

    PluginUpdateHeader(Main main, List<PluginName> namess) {
        super(new MigLayout("ins 0, gap 0, fill", "[grow][][]", "[][grow]"));

        this.pluginHandler = main.pluginHandler;
        this.status = pluginHandler.AVAILABLE_UPDATES.isEmpty() && pluginHandler.INCOMPATIBLE_UPDATES.isEmpty()
                ? "You are all up to date, " : "";
        this.names = namess;

        title = new Title();
        updateAllButton = new UpdateAllButton();
        progressBar = new UpdateProgressBar();

        add(title);
        add(updateAllButton);
        add(new CheckUpdateButton());
        add(progressBar, "dock south, spanx");
    }

    private class Title extends JLabel {
        private Title() {
            super(titleFormatter.format(new Object[]{status, dateFormatter.format(lastChecked)}));
        }
    }

    private class CheckUpdateButton extends MainButton {

        private CheckUpdateButton() {
            super("Check for updates");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            pluginHandler.checkUpdates();
            lastChecked = new Date();
            if (pluginHandler.AVAILABLE_UPDATES.isEmpty() && pluginHandler.INCOMPATIBLE_UPDATES.isEmpty()) {
                status = "You are all up to date";
                updateAllButton.setVisible(false);
            } else {
                status = "";
                updateAllButton.setVisible(!pluginHandler.AVAILABLE_UPDATES.isEmpty());
            }
            title.setText(titleFormatter.format(new Object[]{status, dateFormatter.format(lastChecked)}));
        }
    }

    private class UpdateAllButton extends MainButton {

        private UpdateAllButton() {
            super("Update all");
            setVisible(!pluginHandler.AVAILABLE_UPDATES.isEmpty());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            progressBar.setVisible(true);
            double total = pluginHandler.AVAILABLE_UPDATES.size();
            AtomicInteger current = new AtomicInteger();

            names.forEach(n -> {
                n.update();
                n.task.addPropertyChangeListener(l -> {
                    if ("progress".equals(l.getPropertyName())) {
                        current.addAndGet((int) (25 / total));
                        progressBar.setValue(current.get());
                    }
                });
            });
            while (!names.stream().allMatch(n -> n.canReload));
            names.forEach(n -> n.task.makeReloadable());
            pluginHandler.updatePlugins();
            names.forEach(n -> n.task.finishedReloading());
        }
    }

}
