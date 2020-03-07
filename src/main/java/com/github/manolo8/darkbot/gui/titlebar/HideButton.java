package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class HideButton extends TitleBarButton<JFrame> {

    HideButton(JFrame frame) {
        super(UIUtils.getIcon("close"), frame);
        super.actionColor = UIUtils.RED;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        frame.setVisible(false);
    }
}
