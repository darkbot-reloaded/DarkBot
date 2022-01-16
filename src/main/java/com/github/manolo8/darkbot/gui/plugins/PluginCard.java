package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.extensions.plugins.PluginDefinition;
import com.github.manolo8.darkbot.extensions.plugins.PluginUpdater;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.utils.I18n;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.font.TextAttribute;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class PluginCard extends GenericFeaturesCard {

    private final JProgressBar progressBar;
    private final JLabel progressLabel;
    private final UpdateButton updateButton;

    private final Plugin plugin;
    private final PluginUpdater pluginUpdater;

    PluginCard(Main main, Plugin plugin, FeatureRegistry featureRegistry) {
        setColor(plugin.getIssues().getLevel());
        plugin.getIssues().addUIListener(issues -> setColor(issues.getLevel()));

        this.plugin = plugin;
        this.pluginUpdater = main.pluginUpdater;
        this.progressBar = new JProgressBar();
        this.progressLabel = new JLabel();
        this.updateButton = new UpdateButton();

        progressBar.setVisible(false);
        progressBar.setBorderPainted(false);

        add(progressBar, "hidemode 2, dock south, spanx");
        add(progressLabel, "hidemode 2, dock south, spanx, gapleft 5px");
        add(new PluginSidePanel(plugin), "hidemode 2, dock east");
        add(new PluginName(plugin.getDefinition(), updateButton), "dock north");

        featureRegistry.getFeatures(plugin).forEach(fd -> this.addFeature(main, fd));
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setProgressBarMaximum(int max) {
        progressBar.setMaximum(max);
    }

    public void setUpdateProgress(UpdateStatus status) {
        if (status == UpdateStatus.STARTING) {
            updateButton.setEnabled(false);
            progressBar.setVisible(true);
        }
        progressLabel.setText(I18n.get("plugins.update." + status.toString()));
        progressBar.setValue(status.progress);
    }

    public enum UpdateStatus {
        STARTING(0),
        SAVING_OLD(1),
        DOWNLOADING(2),
        RELOADING(3),
        INDIVIDUALLY_DONE(3),
        DONE(4),
        FAILED(4);

        private final int progress;
        UpdateStatus(int progress) {
            this.progress = progress;
        }

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    class UpdateButton extends MainButton {

        private UpdateButton() {
            super(I18n.get("plugins.config_button.update"));

            setVisible(plugin.getUpdateStatus() == Plugin.UpdateStatus.AVAILABLE);
            if (plugin.getUpdateStatus() == Plugin.UpdateStatus.AVAILABLE)
                setToolTipText(I18n.get("plugins.config_button.update.desc",
                        plugin.getDefinition().version, plugin.getUpdateDefinition().version));

            // Display as a link, blue & underlined plain-text
            setContentAreaFilled(false);
            setForeground(UIManager.getColor("Component.linkColor"));
            setFont(getFont().deriveFont(UNDERLINE));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            pluginUpdater.update(plugin);
        }
    }

    private static class PluginName extends JPanel {
        PluginName(PluginDefinition definition, JComponent updateButton) {
            super(new MigLayout("", "[]5px[]5px[]5px[]20px[]"));

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
            add(updateButton, "hidemode 2, height 16!");
            setOpaque(false);
        }
    }

}
