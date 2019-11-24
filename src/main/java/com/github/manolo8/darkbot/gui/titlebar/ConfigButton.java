package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;

import java.awt.event.ActionEvent;

public class ConfigButton extends TitleBarButton<MainGui> {

    private boolean visible;

    ConfigButton(MainGui frame) {
        super(UIUtils.getIcon("config"), frame);
        frame.addConfigVisibilityListener(v -> {
            visible = v;
            setBackground();
        });
        setToolTipText(I18n.get("gui.config_button"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        frame.toggleConfig();
    }

    protected void setBackground() {
        if (visible) setBackground(actionColor.darker());
        else super.setBackground();
    }

}
