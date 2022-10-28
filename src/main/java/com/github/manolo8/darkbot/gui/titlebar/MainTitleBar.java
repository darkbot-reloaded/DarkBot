package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class MainTitleBar extends JPanel implements SimpleMouseListener {

    private final DragArea.Info info;

    public MainTitleBar(Main main, MainGui frame) {
        super(new MigLayout("ins 0, gap 0, fill", "[][][][][grow, 30px::][][][][][][][]", "[]"));

        add(new ExtraButton(main, frame), "grow");
        add(new ConfigButton(frame), "grow");
        add(new StatsButton(frame), "grow, hidemode 2");
        add(new StartButton(main, frame), "grow");

        add(info = new DragArea.Info(frame), "grow");

        add(new HookButton(frame), "grow, hidemode 2");
        add(new DiagnosticsButton(main, frame), "grow");
        add(new VisibilityButton(main, frame), "grow");
        add(new PinButton(frame), "grow");
        add(new TrayButton(main, frame), "grow, hidemode 2");
        add(new MinimizeButton(frame), "grow");
        add(new MaximizeButton(frame), "grow");
        add(new CloseButton(frame), "grow");
    }

    public void setInfo(String info) {
        this.info.setInfo(info);
    }

}
