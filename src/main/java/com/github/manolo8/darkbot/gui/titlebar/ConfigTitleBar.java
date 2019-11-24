package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigTitleBar extends JPanel implements SimpleMouseListener {

    public ConfigTitleBar(JFrame frame, List<JComponent> tabs, MainButton plugins, Main main) {
        super(new MigLayout("ins 0, gap 0, fill", Stream.generate(() -> "[]").limit(tabs.size()).collect(Collectors.joining()) + "[grow, 30px::][][]", "[]"));
        setBorder(UIUtils.getPartialBorder(0, 1, 0, 0));

        for (JComponent tab : tabs) {
            add(tab, "grow");
        }
        add(new DragArea(frame), "grow");
        add(plugins, "grow");
        add(new PluginReloadButton(frame, main.pluginHandler), "grow");
        add(new HideButton(frame), "grow");
    }

}
