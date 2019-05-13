package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.SystemUtils;

import java.awt.event.ActionEvent;

public class StatsButton extends TitleBarButton<MainGui> {

    StatsButton(MainGui frame) {
        super(UIUtils.getIcon("stats"), frame);
        setToolTipText("Open stats view");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

}
