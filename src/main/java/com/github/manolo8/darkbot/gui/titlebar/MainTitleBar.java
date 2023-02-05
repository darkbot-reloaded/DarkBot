package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;

import javax.swing.*;
import java.awt.*;

public class MainTitleBar extends JMenuBar implements SimpleMouseListener {

    private final Info info;

    public MainTitleBar(Main main, MainGui frame) {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBorder(BorderFactory.createEmptyBorder());

        add(new ExtraButton(main, frame));
        add(new ConfigButton(frame));
        add(new StatsButton(frame));
        add(new StartButton(main, frame));
        add(new BackpageButton(main, frame));

        add(this.info = new Info());

        add(new HookButton(frame));
        add(new DiagnosticsButton(main, frame));
        add(new VisibilityButton(main, frame));
        add(new PinButton(frame));
        add(new TrayButton(main, frame));
    }

    public void setInfo(String info) {
        this.info.label.setText(info);
    }

    private static class Info extends Box.Filler {

        private final JLabel label = new JLabel();

        Info() {
            super(new Dimension(20, 0), new Dimension(120, 0), new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            add(this.label, BorderLayout.CENTER);
            label.setHorizontalAlignment(SwingConstants.CENTER);
        }

    }

}
