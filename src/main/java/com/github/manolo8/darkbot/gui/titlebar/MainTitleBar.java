package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class MainTitleBar extends JMenuBar implements SimpleMouseListener {

    private final TitleFiller titleFiller = new TitleFiller();

    public MainTitleBar(Main main, MainGui frame) {
        setLayout(new MigLayout("fill, ins 0, gap 0, hidemode 2"));
        setBorder(BorderFactory.createEmptyBorder());

        add(new ExtraButton(main, frame));
        add(new ConfigButton(frame));
        add(new StatsButton(frame));
        add(new StartButton(main, frame));
        add(new BackpageButton(main, frame));

        add(titleFiller, "grow, push");
        add(new DiagnosticBar(main), "hmax 30");

        add(new HookButton(frame));
        add(new VisibilityButton(main, frame));
        add(new PinButton(frame));
        add(new TrayButton(main, frame));
    }

    public void setInfo(String info) {
        this.titleFiller.title.setText(info);
    }

    private static class TitleFiller extends Box.Filler {
        private final JLabel title = new JLabel();

        private TitleFiller() {
            super(new Dimension(), new Dimension(), new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

            add(title, BorderLayout.CENTER);
            title.setHorizontalAlignment(SwingConstants.CENTER);
        }
    }
}
