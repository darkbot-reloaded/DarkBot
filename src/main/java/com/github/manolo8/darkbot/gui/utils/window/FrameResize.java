package com.github.manolo8.darkbot.gui.utils.window;

import javax.swing.*;
import java.awt.*;

public class FrameResize extends JComponent {

    private JFrame parentFrame;
    private Insets insets;

    public FrameResize(JFrame parentFrame, Insets insets) {
        this.insets = insets;
        this.parentFrame = parentFrame;

        ComponentBorderDragger dragger = new ComponentBorderDragger(parentFrame, insets, new Dimension(100, 100));
        addMouseListener(dragger);
        addMouseMotionListener(dragger);
    }

    @Override
    public boolean contains(int x, int y) {
        if (WindowUtils.isMaximized(parentFrame)) return false;
        return x < insets.left || y < insets.top || getHeight() - y <= insets.bottom || getWidth() - x <= insets.right;
    }
}
