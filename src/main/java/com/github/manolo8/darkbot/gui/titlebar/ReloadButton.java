package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ReloadButton extends TitleBarButton<JFrame> {

    private PluginHandler pluginHandler;

    ReloadButton(JFrame frame, PluginHandler pluginHandler) {
        super(UIUtils.getIcon("reload"), frame);
        setToolTipText("Reload plugins");
        this.pluginHandler = pluginHandler;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        pluginHandler.updatePlugins();
    }
}
