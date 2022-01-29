package com.github.manolo8.darkbot.gui.utils;

import javax.swing.*;
import java.awt.*;

public class WidthEnforcedScrollPane extends JScrollPane {

    public WidthEnforcedScrollPane(Component view) {
        super(view, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension min = super.getMinimumSize();

        Dimension pref = viewport.getView().getPreferredSize();
        int width = pref.width;
        if (verticalScrollBar != null && pref.height > getBounds().height)
            width += verticalScrollBar.getMinimumSize().width;

        min.width = Math.max(min.width, width);
        return min;
    }
}
