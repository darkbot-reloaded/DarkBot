package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static com.github.manolo8.darkbot.Main.API;

public class VisibilityButton extends TitleBarButton<JFrame> {

    private boolean visible = true;
    private static final Icon SHOW = UIUtils.getIcon("visibility"), HIDE = UIUtils.getIcon("visibility-off");

    VisibilityButton(JFrame frame) {
        super(HIDE, frame);
        setToolTipText(I18n.get("gui.visibility_button"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        API.setVisible(visible = !visible);
        setIcon(visible ? HIDE : SHOW);
    }

}
