package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.extensions.plugins.PluginDefinition;
import com.github.manolo8.darkbot.extensions.plugins.PluginUpdater;
import com.github.manolo8.darkbot.gui.components.MainButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

class PluginName extends JPanel {

    private final PluginUpdater pluginUpdater;

    private final Plugin plugin;

    PluginName(Main main, Plugin plugin) {
        super(new MigLayout("", "[]5px[]5px[]5px[grow][]"));

        this.pluginUpdater = main.pluginUpdater;
        this.plugin = plugin;

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
        add(plugin.getUpdateIssues().getIssues().isEmpty()
                ? new UpdateButton()
                : new IssueList(plugin.getUpdateIssues(), false));
        setOpaque(false);
    }

    //todoo i18n
    private class UpdateButton extends MainButton {

        private UpdateButton() {
            super("Update");
            setVisible(plugin.getUpdateStatus() == Plugin.UpdateStatus.AVAILABLE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            pluginUpdater.update(plugin);
        }
    }

}
