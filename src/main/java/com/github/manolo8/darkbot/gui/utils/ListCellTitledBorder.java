package com.github.manolo8.darkbot.gui.utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.border.Border;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

/**
 * @author Karl Tauber
 * Source: https://github.com/JFormDesigner/FlatLaf/blob/main/flatlaf-demo/src/main/java/com/formdev/flatlaf/demo/intellijthemes/ListCellTitledBorder.java
 */
public class ListCellTitledBorder implements Border {
    private final JList<?> list;
    private final String title;

    public ListCellTitledBorder(JList<?> list, String title) {
        this.list = list;
        this.title = title;
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        int height = c.getFontMetrics(list.getFont()).getHeight();
        return new Insets(height, 0, 0, 0);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        FontMetrics fm = c.getFontMetrics(list.getFont());
        int titleWidth = fm.stringWidth(title);
        int titleHeight = fm.getHeight();

        // fill background
        g.setColor(list.getBackground());
        g.fillRect(x, y, width, titleHeight);

        int gap = UIScale.scale(4);

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);

            g2.setColor(UIManager.getColor("Label.disabledForeground"));

            // paint separator lines
            int sepWidth = (width - titleWidth) / 2 - gap - gap;
            if (sepWidth > 0) {
                int sy = y + Math.round(titleHeight / 2f);
                float sepHeight = UIScale.scale((float) 1);

                g2.fill(new Rectangle2D.Float(x + gap, sy, sepWidth, sepHeight));
                g2.fill(new Rectangle2D.Float(x + width - gap - sepWidth, sy, sepWidth, sepHeight));
            }

            // draw title
            int xt = x + ((width - titleWidth) / 2);
            int yt = y + fm.getAscent();

            FlatUIUtils.drawString(list, g2, title, xt, yt);
        } finally {
            g2.dispose();
        }
    }
}
