package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;

import javax.swing.*;
import java.awt.*;

public class MainTitleBar extends JMenuBar implements SimpleMouseListener {

    private final DragArea.Info info;

    public MainTitleBar(Main main, MainGui frame) {
        JToolBar leftTools = new JToolBar();
        leftTools.add(new ExtraButton(main, frame), "grow");
        leftTools.add(new ConfigButton(frame), "grow");
        leftTools.add(new StatsButton(frame), "grow, hidemode 2");
        leftTools.add(new StartButton(main, frame), "grow");

        JToolBar rightTools = new JToolBar();
        rightTools.add(new HookButton(frame), "grow, hidemode 2");
        rightTools.add(new DiagnosticsButton(main, frame), "grow");
        rightTools.add(new VisibilityButton(main, frame), "grow");
        rightTools.add(new PinButton(frame), "grow");
        rightTools.add(new TrayButton(main, frame), "grow, hidemode 2");
        //rightTools.add(new MinimizeButton(frame), "grow");
        //rightTools.add(new MaximizeButton(frame), "grow");
        //rightTools.add(new CloseButton(frame), "grow");

        add(leftTools);
        add(this.info = new DragArea.Info());
        add(rightTools);

        setMargin(new Insets(0,0,0,0));
    }

    public void setInfo(String info) {
        this.info.setInfo(info);
    }

}
