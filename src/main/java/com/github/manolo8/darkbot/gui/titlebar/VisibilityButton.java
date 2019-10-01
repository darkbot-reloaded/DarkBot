package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static com.github.manolo8.darkbot.Main.API;

public class VisibilityButton extends TitleBarButton<JFrame> {

    private boolean visible = true;
    private static final Icon SHOW = UIUtils.getIcon("visibility"), HIDE = UIUtils.getIcon("visibility-off");

    VisibilityButton(JFrame frame) {
        super(HIDE, frame);
        setToolTipText("Show/Hide browser");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        API.setVisible(visible = !visible);
        API.setRender(visible);
        setIcon(visible ? HIDE : SHOW);
    }

}
