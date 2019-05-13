package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class HideButton extends TitleBarButton<JFrame> {

    HideButton(JFrame frame) {
        super(UIUtils.getIcon("close"), frame);
        super.actionColor = Color.decode("#6E2B28");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        frame.setVisible(false);
    }
}
