package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import java.awt.event.ActionEvent;

public class CloseButton extends TitleBarButton<MainGui> {

    CloseButton(MainGui main) {
        super(UIUtils.getIcon("close"), main);
        super.actionColor = UIUtils.RED;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        frame.tryClose();
    }

}
