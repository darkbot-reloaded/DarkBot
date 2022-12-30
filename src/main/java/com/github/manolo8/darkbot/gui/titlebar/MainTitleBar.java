package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;

import javax.swing.*;

public class MainTitleBar extends JMenuBar implements SimpleMouseListener {

    private final Info info;

    public MainTitleBar(Main main, MainGui frame) {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBorder(BorderFactory.createEmptyBorder());

        add(new ExtraButton(main, frame));
        add(new ConfigButton(frame));
        add(new StatsButton(frame));
        add(new StartButton(main, frame));

        add(Box.createHorizontalGlue());
        add(this.info = new Info());
        add(Box.createHorizontalGlue());

        add(new HookButton(frame));
        add(new DiagnosticsButton(main, frame));
        add(new VisibilityButton(main, frame));
        add(new PinButton(frame));
        add(new TrayButton(main, frame));
    }

    public void setInfo(String info) {
        this.info.setText(info);
    }

    private static class Info extends JLabel {
        public Info() {
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        }
    }

}
