package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TrayButton extends TitleBarButton<JFrame> {

    private final TrayIcon icon;
    private final JPopupMenu popupMenu;
    private final JDialog dialog;

    private boolean shownMsg;
    private Main main;

    TrayButton(Main main, JFrame frame) {
        super(UIUtils.getIcon("tray"), frame);
        setToolTipText(I18n.get("gui.tray_button"));
        setVisible(SystemTray.isSupported());

        this.main = main;
        popupMenu = createPopup();
        dialog = createDialog();
        icon = createTrayIcon();
    }

    private TrayIcon createTrayIcon() {
        TrayIcon icon = new TrayIcon(MainGui.ICON, "DarkBot");
        icon.setImageAutoSize(true);

        icon.addActionListener(l -> {
            SystemTray.getSystemTray().remove(icon);
            frame.setVisible(true);
            super.setVisible(true);
        });

        icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.setLocation(e.getX(), e.getY() - popupMenu.getPreferredSize().height);
                    popupMenu.setInvoker(popupMenu);
                    dialog.setLocation(e.getX(), e.getY());

                    dialog.setVisible(true);
                    popupMenu.setVisible(true);
                }
            }
        });
        return icon;
    }

    // Using JPopupMenu because Look And Feel doesn't apply to PopupMenu
    private JPopupMenu createPopup() {
        JPopupMenu popup = new JPopupMenu("DarkBot");

        JMenuItem title = new JMenuItem("DarkBot", UIUtils.getIcon("icon"));
        JMenuItem quit = new JMenuItem("Quit DarkBot");

        title.setEnabled(false);
        title.setDisabledIcon(UIUtils.getIcon("icon"));
        quit.addActionListener(l -> {
            System.out.println("Exit button pressed, exiting");
            System.exit(0);
        });

        popup.add(title);
        popup.add(new JPopupMenu.Separator());
        new ExtraButton.DefaultExtraMenuProvider().getExtraMenuItems(main).forEach(popup::add);
        popup.add(new JPopupMenu.Separator());
        popup.add(quit);

        return popup;
    }

    /**
     * Creates an invisible JDialog, used as a "hack" to make JPopupMenu invisible when it loses focus
     * @return an invisible JDialog
     * @see <a href="https://stackoverflow.com/a/20079304">https://stackoverflow.com/questions/19868209/cannot-hide-systemtray-jpopupmenu-when-it-loses-focus</a>
     */
    private JDialog createDialog() {
        JDialog dialog = new JDialog();
        dialog.getRootPane().setOpaque(false);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color (0, 0, 0, 0));

        dialog.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                setVisible(false);
                popupMenu.setVisible(false);
            }
        });
        return dialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String username = HeroManager.instance.playerInfo.username;
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
