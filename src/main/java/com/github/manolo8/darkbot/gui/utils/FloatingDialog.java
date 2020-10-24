package com.github.manolo8.darkbot.gui.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FloatingDialog extends JDialog {

    public FloatingDialog(JPanel root, Point p) {
        add(root);

        setUndecorated(true);
        setAlwaysOnTop(true);
        requestFocusInWindow();

        pack();
        setLocation(p);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                root.setVisible(false);
                FloatingDialog.this.dispose();
            }
        });
    }

    public static void show(JPanel panel, JComponent parent) {
        Point p = parent.getLocationOnScreen();
        p.translate(0, parent.getHeight());
        show(panel, p);
    }

    public static void show(JPanel panel, Point p) {
        new FloatingDialog(panel, p).setVisible(true);
    }

}
