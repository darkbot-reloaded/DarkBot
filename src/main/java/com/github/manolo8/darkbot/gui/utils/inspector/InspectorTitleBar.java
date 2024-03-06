package com.github.manolo8.darkbot.gui.utils.inspector;

import com.github.manolo8.darkbot.gui.titlebar.PinButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class InspectorTitleBar extends JMenuBar {

    public InspectorTitleBar(JFrame frame) {
        setLayout(new MigLayout("fill, ins 0, gap 0"));
        setBorder(BorderFactory.createEmptyBorder());

        add(Box.createGlue(), "grow, push");
        add(new PinButton(frame) {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setAlwaysOnTop(isSelected());
            }
        });
    }
}
