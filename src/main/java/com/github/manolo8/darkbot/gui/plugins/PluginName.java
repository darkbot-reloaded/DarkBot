package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.extensions.plugins.PluginDefinition;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginIssue;
import com.github.manolo8.darkbot.gui.components.MainButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

class PluginName extends JPanel {

    private final PluginHandler pluginHandler;
    private final Plugin plugin;
    private final PluginCard pluginCard;

    final JButton updateButton;

    PluginName(Main main, PluginCard card) {
        super(new MigLayout("", "[]5px[]5px[]5px[grow][]"));

        this.pluginHandler = main.pluginHandler;
        this.plugin = card.plugin;
        this.pluginCard = card;

        updateButton = new UpdateButton();

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
        add(pluginHandler.INCOMPATIBLE_UPDATES.contains(plugin)
                ? new IssueList(pluginHandler.INCOMPATIBLE_UPDATES.stream()
                    .filter(pl -> pl.equals(plugin))
                    .findFirst().get().getIssues(), i -> i.getLevel() == PluginIssue.Level.ERROR,false)
                : updateButton);
        setOpaque(false);
    }

    //todoo i18n
    private class UpdateButton extends MainButton {

        private UpdateButton() {
            super("Update");
            setVisible(pluginHandler.AVAILABLE_UPDATES.contains(plugin));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            pluginCard.update();
            pluginCard.updateTask.makeReloadable();
            while (!pluginCard.updateTask.canReload);
            pluginHandler.updatePlugins();
            pluginCard.updateTask.finishedReloading();
        }
    }

}
