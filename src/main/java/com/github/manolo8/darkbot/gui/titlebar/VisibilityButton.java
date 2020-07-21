package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.components.ApiSettings;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.github.manolo8.darkbot.Main.API;

public class VisibilityButton extends TitleBarToggleButton<JFrame> {

    private static final Icon SHOW = UIUtils.getIcon("visibility"), HIDE = UIUtils.getIcon("visibility-off");

    private final Main main;

    VisibilityButton(Main main, JFrame frame) {
        super(SHOW, frame);
        this.main = main;

        setSelectedIcon(HIDE);
        setToolTipText(I18n.get("gui.visibility_button"));
        setSelected(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e)) return;
                onRightClick();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        toggleVisibility(main.config.BOT_SETTINGS.FULLY_HIDE_API, isSelected());
    }

    private void toggleVisibility(boolean minimizing, boolean visible) {
        if (minimizing) API.setMinimized(!visible);
        else API.setVisible(visible);
    }

    private void onRightClick() {
        Point p = getLocationOnScreen();
        p.translate(0, getHeight());
        new ApiSettings(main, p);
    }

}
