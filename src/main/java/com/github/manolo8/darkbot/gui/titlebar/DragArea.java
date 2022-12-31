package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.window.WindowUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class DragArea extends JComponent implements SimpleMouseListener {

    private final JFrame frame;

    // X and Y in relation to the DragArea component location, range 0 to 1 (percent)
    private double internalX, internalY;

    DragArea(JFrame frame) {
        this.frame = frame;

        setMinimumSize(new Dimension(0, 0));
        setPreferredSize(new Dimension(0, 0));
        setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        setBorder(UIUtils.getPartialBorder(1, 0, 1, 0));
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point frameLoc = frame.getLocationOnScreen();
        Point clickLoc = e.getLocationOnScreen();
        internalX = (clickLoc.x - frameLoc.x) / (double) frame.getWidth();
        internalY = (clickLoc.y - frameLoc.y) / (double) frame.getHeight();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        WindowUtils.setMaximized(frame, false);

        Point mouseLoc = e.getLocationOnScreen();
        frame.setLocation(mouseLoc.x - (int)(internalX * frame.getWidth()),
                mouseLoc.y - (int) (internalY * frame.getHeight()));
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
