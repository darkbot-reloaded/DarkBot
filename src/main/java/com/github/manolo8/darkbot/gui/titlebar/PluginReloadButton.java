package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PluginReloadButton extends TitleBarButton<JFrame> {

    private PluginHandler pluginHandler;

    PluginReloadButton(JFrame frame, PluginHandler pluginHandler) {
        super(UIUtils.getIcon("reload"), frame);
        setToolTipText(I18n.get("plugins.reload"));
        this.pluginHandler = pluginHandler;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        pluginHandler.updatePlugins();
    }
}
