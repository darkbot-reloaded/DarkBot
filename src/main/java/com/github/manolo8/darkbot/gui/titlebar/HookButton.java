package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.api.adapters.DarkMemAdapter;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class HookButton extends TitleBarButton<JFrame> {

    HookButton(JFrame frame) {
        super(UIUtils.getIcon("plug"), frame);

        setVisible(Main.API instanceof DarkMemAdapter);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Main.API.createWindow();
    }

}
