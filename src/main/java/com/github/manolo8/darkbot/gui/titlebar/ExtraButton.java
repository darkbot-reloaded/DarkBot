package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.utils.PopupMenuListenerAdapter;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.SystemUtils;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;

public class ExtraButton extends TitleBarToggleButton<JFrame> {

    private JPopupMenu extraOptions = new JPopupMenu("Extra Options");

    ExtraButton(Main main, JFrame frame) {
        super(UIUtils.getIcon("hamburger"), frame);

        JMenuItem home = new JMenuItem(I18n.get("gui.hamburger_button.home")),
                reload = new JMenuItem(I18n.get("gui.hamburger_button.reload")),
                copySid = new JMenuItem(I18n.get("gui.hamburger_button.copy_sid")),
                discord = new JMenuItem(I18n.get("gui.hamburger_button.discord"));


        extraOptions.add(home);
        extraOptions.add(reload);
        extraOptions.add(copySid);
        extraOptions.add(discord);
        home.addActionListener(e -> {
            String sid = main.statsManager.sid, instance = main.statsManager.instance;
            if (sid == null || sid.isEmpty() || instance == null || instance.isEmpty()) return;
            String url = instance + "?dosid=" + sid;
            if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) SystemUtils.toClipboard(url);
            else SystemUtils.openUrl(url);
        });
        reload.addActionListener(e -> {
            System.out.println("Triggering refresh: user requested");
            Main.API.handleRefresh();
        });
        discord.addActionListener(e -> SystemUtils.openUrl("https://discord.gg/KFd8vZT"));
        copySid.addActionListener(e -> SystemUtils.toClipboard(main.statsManager.sid));

        extraOptions.setBorder(UIUtils.getBorder());

        extraOptions.addPopupMenuListener(new PopupMenuListenerAdapter() {
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                setSelected(false);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isSelected()) extraOptions.show(this, 0, getHeight() - 1);
    }

}
