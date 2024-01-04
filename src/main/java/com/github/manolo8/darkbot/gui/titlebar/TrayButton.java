package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TrayButton extends TitleBarButton<JFrame> {

    private final Main main;
    // Default PopupMenu for TrayIcon has no look & feel, forcing us to use a JPopupMenu
    private final JPopupMenu popupMenu;
    // We use a JDialog to make JPopupMenu disappear when it loses focus, see https://stackoverflow.com/a/20079304
    private final JDialog dialog;
    private final @Nullable TrayIcon icon;

    private boolean shownMsg;

    TrayButton(Main main, JFrame frame) {
        super(UIUtils.getIcon("tray"), frame);
        setToolTipText(I18n.get("gui.tray_button"));
        setVisible(SystemTray.isSupported());

        this.main = main;
        popupMenu = new JPopupMenu("DarkBot");
        rebuildPopup();
        dialog = createDialog();
        icon = SystemTray.isSupported() ? createTrayIcon() : null;
    }

    private TrayIcon createTrayIcon() {
        TrayIcon icon = new TrayIcon(MainGui.ICON, "DarkBot");
        icon.setImageAutoSize(true);

        icon.addActionListener(l -> {
            SystemTray.getSystemTray().remove(icon);
            frame.setVisible(true);
        });

        icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e)) return;
                rebuildPopup();
                popupMenu.setLocation(e.getX(), e.getY() - popupMenu.getPreferredSize().height);
                popupMenu.setInvoker(popupMenu);
                dialog.setLocation(e.getX(), e.getY());

                dialog.setVisible(true);
                popupMenu.setVisible(true);
            }
        });
        return icon;
    }

    private void rebuildPopup() {
        popupMenu.removeAll();

        JMenuItem title = new JMenuItem("DarkBot", UIUtils.getIcon("icon"));
        JMenuItem quit = new JMenuItem(I18n.get("gui.tray_menu.quit"));

        title.setEnabled(false);
        title.setDisabledIcon(UIUtils.getIcon("icon"));
        quit.addActionListener(l -> {
            System.out.println("Tray icon exit button pressed, exiting");
            System.exit(0);
        });

        popupMenu.add(title);
        popupMenu.add(new JPopupMenu.Separator());
        new ExtraButton.DefaultExtraMenuProvider()
                .getExtraMenuItems(main.pluginAPI).forEach(popupMenu::add);
        popupMenu.add(new JPopupMenu.Separator());
        popupMenu.add(quit);
    }

    private JDialog createDialog() {
        JDialog dialog = new JDialog();
        dialog.getRootPane().setOpaque(false);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color (0, 0, 0, 0));

        dialog.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                dialog.setVisible(false);
                popupMenu.setVisible(false);
            }
        });
        return dialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (icon == null) return;
        String username = Main.INSTANCE.hero.playerInfo.username;
        if (!username.isEmpty())
            icon.setToolTip("DarkBot - " + username);
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
