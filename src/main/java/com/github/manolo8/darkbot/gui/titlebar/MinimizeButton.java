package com.github.manolo8.darkbot.gui.titlebar;


import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MinimizeButton extends TitleBarButton<JFrame> {

    MinimizeButton(JFrame frame) {
        super(UIUtils.getIcon("minimize"), frame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        frame.setState(Frame.ICONIFIED);
    }
}
