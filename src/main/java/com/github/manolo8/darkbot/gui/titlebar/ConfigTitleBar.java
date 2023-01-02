package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ConfigTitleBar extends JMenuBar implements SimpleMouseListener {

    public ConfigTitleBar(JFrame frame, List<JComponent> tabs, ConfigPicker configs, AbstractButton plugins, Main main) {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBorder(BorderFactory.createEmptyBorder());

        for (JComponent tab : tabs)
            add(tab);

        add(configs);
        add(new Box.Filler(new Dimension(20, 0),
                new Dimension(20, 0),
                new Dimension(Short.MAX_VALUE, Short.MAX_VALUE)));
        add(plugins);
        add(new PluginReloadButton(frame, main.pluginHandler));
    }

}
