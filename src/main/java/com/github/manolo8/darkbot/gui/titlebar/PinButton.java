package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PinButton extends TitleBarButton<JFrame> {

    private static final Icon PIN = UIUtils.getIcon("pin"), UNPIN = UIUtils.getIcon("unpin");

    PinButton(JFrame frame) {
        super(PIN, frame);
        setToolTipText("Always on top");
        setBackground();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        frame.setAlwaysOnTop(!frame.isAlwaysOnTop());
        ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.DISPLAY.ALWAYS_ON_TOP = frame.isAlwaysOnTop();
        ConfigEntity.changed();
    }

    protected void setBackground() {
        if (ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.DISPLAY.ALWAYS_ON_TOP) {
            setBackground(actionColor.darker());
            setIcon(UNPIN);
        } else {
            super.setBackground();
            setIcon(PIN);
        }
    }

}
