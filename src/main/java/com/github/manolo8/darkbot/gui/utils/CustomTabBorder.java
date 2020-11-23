package com.github.manolo8.darkbot.gui.utils;

import com.formdev.flatlaf.ui.FlatButtonBorder;

import java.awt.*;

public class CustomTabBorder extends FlatButtonBorder {

    private final Color highlight = UIUtils.TAB_HIGLIGHT;
    private final int top, left, bottom, right;

    public CustomTabBorder(int top, int left, int bottom, int right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        super.paintBorder(c, g, x, y, width, height);

        g.setColor(highlight);
        if (top > 0) g.fillRect(0, 0, width, top);
        if (left > 0) g.fillRect(0, 0, left, height);
        if (bottom > 0) g.fillRect(0, height - bottom, width, height);
        if (right > 0) g.fillRect(width - right, 0, width, height);
    }
}
