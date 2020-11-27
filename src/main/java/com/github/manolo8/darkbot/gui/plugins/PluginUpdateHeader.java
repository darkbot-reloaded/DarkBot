package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginUpdater;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.utils.I18n;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.stream.Collectors;

public class PluginUpdateHeader extends JPanel {

    private static final MessageFormat titleFormatter = new MessageFormat(I18n.get("plugins.config_button.update_header"));
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, d MMM, hh:mm a", Locale.ROOT);
    private String status;

    private final PluginUpdater pluginUpdater;
    private final PluginHandler pluginHandler;

    private final Title title;
    private final JProgressBar progressBar;
    private final UpdateAllButton updateAllButton;
    private final CheckUpdateButton checkUpdateButton;

    PluginUpdateHeader(Main main) {
        super(new MigLayout("ins 0, gap 0, fill", "[grow][][]", "[][grow]"));

        this.pluginUpdater = main.pluginUpdater;
        this.pluginHandler = main.pluginHandler;
        this.status = !pluginUpdater.hasAnyUpdates() ? I18n.get("plugins.config_button.up_to_date") + " ," : "";
        this.title = new Title();
        this.progressBar = new JProgressBar();
        this.updateAllButton = new UpdateAllButton();
        this.checkUpdateButton = new CheckUpdateButton();

        progressBar.setVisible(false);
        progressBar.setBorderPainted(false);

        add(title);
        add(updateAllButton);
        add(checkUpdateButton);
        add(progressBar, "dock south, spanx");
    }

    void refreshUI() {
        updateAllButton.refresh();
        progressBar.setVisible(false);

        checkUpdateButton.setEnabled(true);
        status = !pluginUpdater.hasAnyUpdates() ? I18n.get("plugins.config_button.up_to_date") + " ," : "";
        title.setText(titleFormatter.format(new Object[]{status, dateFormatter.format(pluginUpdater.getLastChecked())}));
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    private class Title extends JLabel {
        private Title() {
            super(titleFormatter.format(new Object[]{status, dateFormatter.format(pluginUpdater.getLastChecked())}));
        }
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
