package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.extensions.behaviours.BehaviourHandler;
import com.github.manolo8.darkbot.extensions.modules.CustomModule;
import com.github.manolo8.darkbot.extensions.modules.ModuleHandler;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.extensions.plugins.PluginDefinition;
import com.github.manolo8.darkbot.extensions.plugins.PluginIssue;
import com.github.manolo8.darkbot.gui.tree.components.JLabel;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
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
    private ModuleHandler moduleHandler;
    private BehaviourHandler behaviourHandler;

    public PluginCard(Plugin plugin, ModuleHandler moduleHandler, BehaviourHandler behaviourHandler) {
        super(new MigLayout());
        this.plugin = plugin;
        this.moduleHandler = moduleHandler;
        this.behaviourHandler = behaviourHandler;
        setColor();

        PluginDefinition definition = plugin.getDefinition();

        add(new JLabel(definition.name), "split 3");
        add(new JLabel("v" + definition.version.toString()), "gapleft 5px");
        add(new JLabel("by " + definition.author), "gapleft 5px, wrap");
        plugin.getIssues()
                .stream()
                .map(this::getError)
                .forEach(err -> this.add(err, "wrap"));
        Arrays.stream(definition.modules).forEach(m -> add(getModule(m)));
        //add(new JLabel(Arrays.toString(definition.modules)));
    }

    private JLabel getError(PluginIssue pluginIssue) {
        JLabel label = new JLabel(pluginIssue.getMessage());
        label.setToolTipText(pluginIssue.getDescription());
        return label;
    }

    private JCheckBox getModule(String id) {
        CustomModule module = moduleHandler.getFeatureDefinition(id);
        String name = module == null ? id.substring(id.lastIndexOf(".") + 1) : module.name();
        String tooltip = module == null ? "The module class couldn't be loaded" : module.description();

        JCheckBox checkBox = new JCheckBox(name);
        if (!tooltip.isEmpty()) checkBox.setToolTipText(tooltip);
        if (module == null) checkBox.setBackground(ERROR_COLOR);
        else checkBox.setOpaque(false);
        return checkBox;
    }

    private void setColor() {
        if (!plugin.canLoad()) {
            setBorder(ERROR_BORDER);
            setBackground(ERROR_COLOR);
        } else if (!plugin.getIssues().isEmpty()) {
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
