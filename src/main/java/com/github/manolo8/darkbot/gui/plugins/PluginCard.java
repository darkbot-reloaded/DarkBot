package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.extensions.modules.CustomModule;
import com.github.manolo8.darkbot.extensions.modules.ModuleHandler;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.extensions.plugins.PluginDefinition;
import com.github.manolo8.darkbot.gui.tree.components.JLabel;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Arrays;

public class PluginCard extends JPanel {

    private static final Border LOADED_BORDER = BorderFactory.createLineBorder(UIUtils.GREEN),
            WARNING_BORDER = BorderFactory.createLineBorder(UIUtils.YELLOW),
            ERROR_BORDER = BorderFactory.createLineBorder(UIUtils.RED);
    private static int ALPHA = 32 << 24;
    private static final Color LOADED_COLOR = new Color(UIUtils.GREEN.getRGB() + ALPHA, true),
            WARNING_COLOR = new Color(UIUtils.YELLOW.getRGB() + ALPHA, true),
            ERROR_COLOR = new Color(UIUtils.RED.getRGB() + ALPHA, true);

    private Plugin plugin;
    private FeatureRegistry featureRegistry;

    public PluginCard(Plugin plugin, FeatureRegistry featureRegistry) {
        super(new MigLayout(new LC().fillX().wrapAfter(1).insetsAll("7px"), new AC(), new AC().noGrid(1)));
        this.plugin = plugin;
        this.featureRegistry = featureRegistry;
        setColor();

        PluginDefinition definition = plugin.getDefinition();


        add(new JLabel(definition.name), "split 3");
        add(new JLabel("v" + definition.version.toString()), "gapleft 5px");
        add(new JLabel("by " + definition.author), "gapleft 5px, wrap");
        add(new IssueList(plugin.getIssues()), "dock east");

        featureRegistry.getFeatures(plugin)
                .forEach(fd -> add(getFeature(fd)));
        //add(new JLabel(Arrays.toString(definition.modules)));
    }

    private JCheckBox getFeature(FeatureDefinition feature) {
        JCheckBox checkBox = new JCheckBox(feature.getName());
        if (!feature.getDescription().isEmpty()) checkBox.setToolTipText(feature.getDescription());
        if (feature.getIssues().hasIssues()) checkBox.setBackground(ERROR_COLOR);
        else checkBox.setOpaque(false);
        return checkBox;
    }

    private void setColor() {
        if (!plugin.getIssues().canLoad()) {
            setBorder(ERROR_BORDER);
            setBackground(ERROR_COLOR);
        } else if (plugin.getIssues().hasIssues()) {
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
}
