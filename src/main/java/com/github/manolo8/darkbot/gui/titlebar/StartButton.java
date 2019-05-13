package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class StartButton extends TitleBarButton<JFrame> {

    private static final Icon PLAY = UIUtils.getIcon("play"), PAUSE = UIUtils.getIcon("pause");
    private final Main main;

    StartButton(Main main, JFrame frame) {
        super(PLAY, frame);
        setToolTipText("Start/Pause bot");
        this.main = main;
        main.status.add(running -> setIcon(running ? PAUSE : PLAY));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setIcon(main.isRunning() ? PLAY : PAUSE);
        main.setRunning(!main.isRunning());
    }

}
