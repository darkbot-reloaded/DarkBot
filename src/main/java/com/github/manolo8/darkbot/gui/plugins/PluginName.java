package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.extensions.plugins.PluginDefinition;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.SystemUtils;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

class PluginName extends JPanel {

    PluginName(PluginDefinition definition, JComponent updateButton) {
        super(new MigLayout("", "[]5px[]5px[]5px[]20px[]20px[]"));

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

        if (definition.donation != null)
            add(createDonateButton(definition), "hidemode 2");

        add(updateButton, "hidemode 2, height 16!");
        setOpaque(false);
    }

    private Component createDonateButton(PluginDefinition pd) {
        JButton donateButton = new JButton("Donate " + pd.author, UIUtils.getIcon("donate", 18, 18));
        donateButton.addActionListener(c -> SystemUtils.openUrl(pd.donation.toString()));

        return donateButton;
    }
}
