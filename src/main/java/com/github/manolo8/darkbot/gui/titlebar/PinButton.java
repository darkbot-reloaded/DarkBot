package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PinButton extends TitleBarToggleButton<JFrame> {

    private static final Icon PIN = UIUtils.getIcon("pin"), UNPIN = UIUtils.getIcon("unpin");

    PinButton(JFrame frame) {
        super(PIN, frame);
        setSelectedIcon(UNPIN);
        setToolTipText(I18n.get("gui.pin_button"));
        setSelected(ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.DISPLAY.ALWAYS_ON_TOP);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        frame.setAlwaysOnTop(isSelected());

        ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.DISPLAY.ALWAYS_ON_TOP = isSelected();
        ConfigEntity.changed();
    }

}
