package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TrayButton extends TitleBarButton<JFrame> {

    private final TrayIcon icon;
    private boolean shownMsg;

    TrayButton(JFrame frame) {
        super(UIUtils.getIcon("tray"), frame);
        setToolTipText(I18n.get("gui.tray_button"));
        setVisible(SystemTray.isSupported());
        icon = new TrayIcon(MainGui.ICON, "DarkBot");
        icon.setImageAutoSize(true);
        icon.addActionListener(l -> {
            SystemTray.getSystemTray().remove(icon);
            frame.setVisible(true);
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            SystemTray.getSystemTray().add(icon);
        } catch (Exception ex) {
            ex.printStackTrace();
            setVisible(false); // Disable minimizing to tray
            return;
        }
        if (!shownMsg) {
            icon.displayMessage(null, "Minimized to tray", TrayIcon.MessageType.NONE);
            shownMsg = true;
        }
        frame.setVisible(false);
    }

}
