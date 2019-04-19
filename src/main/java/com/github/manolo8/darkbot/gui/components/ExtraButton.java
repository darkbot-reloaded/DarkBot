package com.github.manolo8.darkbot.gui.components;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.utils.SystemUtils;

import javax.swing.*;
import java.awt.event.MouseEvent;

import static com.github.manolo8.darkbot.Main.API;

public class ExtraButton extends MainButton {

    private JPopupMenu runOptions = new JPopupMenu("Run Options");

    public ExtraButton(Main main) {
        super("   ...   ");

        JMenuItem reload = new JMenuItem("RELOAD"),
                discord = new JMenuItem("DISCORD"),
                copySid = new JMenuItem("COPY SID");

        runOptions.add(reload);
        runOptions.add(copySid);
        runOptions.add(discord);
        reload.addActionListener(e -> API.handleRefresh());
        discord.addActionListener(e -> SystemUtils.openUrl("https://discord.gg/KFd8vZT"));
        copySid.addActionListener(e -> SystemUtils.toClipboard(main.statsManager.sid));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        runOptions.show(this, e.getX(), e.getY());
    }

}
