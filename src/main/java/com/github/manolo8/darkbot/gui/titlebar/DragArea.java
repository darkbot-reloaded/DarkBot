package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.window.WindowUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class DragArea extends JPanel implements SimpleMouseListener {

    private final JFrame frame;
    // X and Y in relation to the DragArea component location, range 0 to 1 (percent)
    private double internalX, internalY;

    DragArea(JFrame main) {
        this.frame = main;

        setBorder(UIUtils.getBorder());
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point dragAreaLoc = getLocationOnScreen();
        Point clickLoc = e.getLocationOnScreen();
        internalX = (clickLoc.x - dragAreaLoc.x) / (double) getWidth();
        internalY = (clickLoc.y - dragAreaLoc.y) / (double) getHeight();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        WindowUtils.setMaximized(frame, false);

        Point clickLoc = e.getLocationOnScreen();
        Point dragAreaLoc = getLocation();
        frame.setLocation(clickLoc.x - (int)(internalX * getWidth()) - dragAreaLoc.x, clickLoc.y - (int) (internalY * getHeight()) - dragAreaLoc.y);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getLocationOnScreen().y == frame.getGraphicsConfiguration().getBounds().y) {
            WindowUtils.setMaximized(frame, true);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) WindowUtils.toggleMaximized(frame);
    }
}
