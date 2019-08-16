package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ConfigTitleBar extends JPanel implements SimpleMouseListener {

    public ConfigTitleBar(JFrame frame, JPanel tabs, MainButton plugins, Main main) {
        super(new MigLayout("ins 0, gap 0, fill", "[][grow, 30px::][][]", "[]"));

        add(tabs);
        add(new DragArea(frame), "grow");
        add(plugins, "grow");
        add(new PluginReloadButton(frame, main.pluginHandler), "grow");
        add(new HideButton(frame), "grow");
    }

}
