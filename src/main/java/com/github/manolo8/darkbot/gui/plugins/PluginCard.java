package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.extensions.plugins.PluginIssue;
import com.github.manolo8.darkbot.extensions.plugins.PluginUpdater;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Locale;

public class PluginCard extends JPanel {

    private static final Border LOADED_BORDER = BorderFactory.createLineBorder(UIUtils.GREEN),
            WARNING_BORDER = BorderFactory.createLineBorder(UIUtils.YELLOW),
            ERROR_BORDER = BorderFactory.createLineBorder(UIUtils.RED);
    private static final int ALPHA = 32 << 24;
    private static final Color LOADED_COLOR = new Color(UIUtils.GREEN.getRGB() + ALPHA, true),
            WARNING_COLOR = new Color(UIUtils.YELLOW.getRGB() + ALPHA, true),
            ERROR_COLOR = new Color(UIUtils.RED.getRGB() + ALPHA, true);

    private final JProgressBar progressBar;
    private final JLabel progressLabel;
    private final UpdateButton updateButton;

    private final Plugin plugin;
    private final PluginUpdater pluginUpdater;

    PluginCard(Main main, Plugin plugin, FeatureRegistry featureRegistry) {
        super(new MigLayout("fillx, gapy 0, ins 0 0 5px 0", "5px[]0px[]10px[]10px[grow]", "[]"));
        setColor(plugin.getIssues());
        plugin.getIssues().addListener(this::setColor);

        this.plugin = plugin;
        this.pluginUpdater = main.pluginUpdater;
        this.progressBar = new JProgressBar();
        this.progressLabel = new JLabel();
        this.updateButton = new UpdateButton();

        progressBar.setVisible(false);
        progressBar.setBorderPainted(false);

        add(progressBar, "dock south, spanx");
        add(progressLabel, "dock south, spanx, gapleft 5px");
        add(new IssueList(plugin.getIssues(), plugin.getUpdateIssues()), "hidemode 2, dock east");
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

    private void addFeature(Main main, FeatureDefinition<?> feature) {
        add(new FeatureTypeButton(feature), "growx");
        if (Configurable.class.isAssignableFrom(feature.getClazz())) {
            //noinspection unchecked
            add(new FeatureConfigButton(main, (FeatureDefinition<Configurable<?>>) feature));
        } else {
            add(new JLabel());
        }
        add(new FeatureCheckbox(feature));
        add(new IssueList(feature.getIssues()), "hidemode 2, wrap");
    }

    private void setColor(IssueHandler issues) {
        if (issues.getLevel() == PluginIssue.Level.ERROR) {
            setBorder(ERROR_BORDER);
            setBackground(ERROR_COLOR);
        } else if (issues.getLevel() == PluginIssue.Level.WARNING) {
            setBorder(WARNING_BORDER);
            setBackground(WARNING_COLOR);
        } else {
            setBorder(LOADED_BORDER);
            setBackground(LOADED_COLOR);
        }
        setOpaque(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        // Panels don't render a background if set to opaque = false
        // But opaque = false is required since the background is not completely opaque.
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    class UpdateButton extends MainButton {

        private UpdateButton() {
            super(I18n.get("plugins.config_button.update"));

            if (plugin.getUpdateStatus() == Plugin.UpdateStatus.AVAILABLE) {
                setVisible(true);
                setToolTipText(I18n.get("plugins.config_button.update.desc",
                        plugin.getDefinition().version,
                        plugin.getUpdateDefinition().version));
            } else setVisible(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            pluginUpdater.update(plugin);
        }
    }
}
