package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.extensions.plugins.PluginDefinition;
import com.github.manolo8.darkbot.extensions.plugins.PluginUpdater;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.utils.I18n;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

class PluginName extends JPanel {

    private final Plugin plugin;
    private final PluginUpdater pluginUpdater;

    private final UpdateButton updateButton;

    PluginName(Plugin plugin, PluginUpdater pluginUpdater) {
        super(new MigLayout("", "[]5px[]5px[]5px[grow][]"));

        this.pluginUpdater = pluginUpdater;
        this.plugin = plugin;
        this.updateButton = new UpdateButton();

        PluginDefinition definition = plugin.getDefinition();
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
        add(updateButton, "hidemode 2");
        setOpaque(false);
    }

    UpdateButton getUpdateButton() {
        return updateButton;
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
