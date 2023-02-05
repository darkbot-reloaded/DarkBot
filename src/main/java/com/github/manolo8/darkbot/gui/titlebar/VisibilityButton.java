package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.components.ApiSettingsPanel;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.github.manolo8.darkbot.Main.API;

public class VisibilityButton extends TitleBarToggleButton<JFrame> {

    private static final Icon SHOW = UIUtils.getIcon("visibility"), HIDE = UIUtils.getIcon("visibility_off");

    private final Main main;

    VisibilityButton(Main main, JFrame frame) {
        super(SHOW, frame);
        this.main = main;

        setSelectedIcon(HIDE);
        setToolTipText(I18n.get("gui.visibility_button"));
        setSelected(API.isInitiallyShown());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e)) return;
                new ApiSettingsPanel(main, main.config.BOT_SETTINGS.API_CONFIG, VisibilityButton.this);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        API.setVisible(isSelected(), main.config.BOT_SETTINGS.API_CONFIG.FULLY_HIDE_API);
    }

}
