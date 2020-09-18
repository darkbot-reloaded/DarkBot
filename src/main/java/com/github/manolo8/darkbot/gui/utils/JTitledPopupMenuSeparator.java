package com.github.manolo8.darkbot.gui.utils;

import com.formdev.flatlaf.util.UIScale;
import sun.swing.SwingUtilities2;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class JTitledPopupMenuSeparator extends JPopupMenu.Separator {

    private final JPopupMenu menu = new JPopupMenu();

    public JTitledPopupMenuSeparator(String title) {
        setBorder(new Border(title));
        setPreferredSize(new Dimension(0, menu.getFontMetrics(menu.getFont()).getHeight()));
    }

    private class Border extends TitledBorder {

        public Border(String title) {
            super(title);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            FontMetrics fm = c.getFontMetrics(menu.getFont());
            int titleWidth = fm.stringWidth(title);
            int titleHeight = fm.getHeight();

            // fill background
            g.setColor(menu.getBackground());
            g.fillRect(x, y, width, titleHeight);

            int gap = UIScale.scale(4);

            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

                SwingUtilities2.drawStringUnderlineCharAt(menu, g2, title, -1, xt, yt);
            } finally {
                g2.dispose();
            }
        }
    }
}