package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.window.WindowUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MaximizeButton extends TitleBarButton<JFrame> {

    MaximizeButton(JFrame frame) {
        super(UIUtils.getIcon("maximize"), frame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WindowUtils.toggleMaximized(frame);
    }

}
