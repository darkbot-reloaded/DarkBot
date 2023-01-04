package com.github.manolo8.darkbot.gui.titlebar;

import com.formdev.flatlaf.ui.FlatButtonUI;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

public class RefreshButton extends JButton {

    public RefreshButton() {
        super(new RefreshIcon());

        setFocusable(false);
        setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder());
        setToolTipText(I18n.get("gui.hamburger_button.reload"));

        addActionListener(l -> {
            System.out.println("Triggering refresh: user requested");
            Main.API.handleRefresh();
        });
    }

    private static class RefreshIcon implements Icon {
        private Path2D refreshPath;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(getColor(c));

            //<!-- Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file. -->
            //<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16">
            //    <path fill="#6E6E6E" fill-rule="evenodd"
            //          d="M12.5747152,11.8852806 C11.4741474,13.1817355 9.83247882,14.0044386 7.99865879,14.0044386 C5.03907292,14.0044386 2.57997332,11.8615894 2.08820756,9.0427473 L3.94774327,9.10768372 C4.43372186,10.8898575 6.06393114,12.2000519 8.00015362,12.2000519 C9.30149237,12.2000519 10.4645985,11.6082097 11.2349873,10.6790094 L9.05000019,8.71167959 L14.0431479,8.44999981 L14.3048222,13.4430431 L12.5747152,11.8852806 Z M3.42785637,4.11741586 C4.52839138,2.82452748 6.16775464,2.00443857 7.99865879,2.00443857 C10.918604,2.00443857 13.3513802,4.09026967 13.8882946,6.8532307 L12.0226389,6.78808057 C11.5024872,5.05935553 9.89838095,3.8000774 8.00015362,3.8000774 C6.69867367,3.8000774 5.53545628,4.39204806 4.76506921,5.32142241 L6.95482203,7.29304326 L1.96167436,7.55472304 L1.70000005,2.56167973 L3.42785637,4.11741586 Z"
            //          transform="rotate(3 8.002 8.004)"/>
            //</svg>

            // svg path converted into java2d
            if (refreshPath == null) {
                refreshPath = new Path2D.Double(Path2D.WIND_EVEN_ODD);
                refreshPath.moveTo(12.57, 11.88);
                refreshPath.curveTo(11.47, 13.18, 9.83, 14.00, 7.99, 14.00);
                refreshPath.curveTo(5.03, 14.00, 2.57, 11.86, 2.08, 9.04);
                refreshPath.lineTo(3.94, 9.10);
                refreshPath.curveTo(4.43, 10.88, 6.06, 12.20, 8.00, 12.20);
                refreshPath.curveTo(9.30, 12.20, 10.46, 11.60, 11.23, 10.67);
                refreshPath.lineTo(9.05, 8.71);
                refreshPath.lineTo(14.04, 8.45);
                refreshPath.lineTo(14.30, 13.44);
                refreshPath.lineTo(12.57, 11.88);
                refreshPath.closePath();

                refreshPath.moveTo(3.42, 4.11);
                refreshPath.curveTo(4.52, 2.82, 6.16, 2.00, 7.99, 2.00);
                refreshPath.curveTo(10.91, 2.00, 13.35, 4.09, 13.88, 6.85);
                refreshPath.lineTo(12.02, 6.78);
                refreshPath.curveTo(11.50, 5.05, 9.89, 3.80, 8.00, 3.80);
                refreshPath.curveTo(6.69, 3.80, 5.53, 4.39, 4.76, 5.32);
                refreshPath.lineTo(6.95, 7.29);
                refreshPath.lineTo(1.96, 7.55);
                refreshPath.lineTo(1.7, 2.56);
                refreshPath.lineTo(3.42, 4.11);
                refreshPath.closePath();
            }

            g2.fill(refreshPath);
        }

        private Color getColor(Component c) {
            Color foreground = c.getForeground();
            return FlatButtonUI.buttonStateColor(c, foreground, null, null,
                    darker(foreground, 0.75), darker(foreground, 0.6));
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }

        private static Color darker(Color c, double factor) {
            return new Color(Math.max((int) (c.getRed() * factor), 0),
                    Math.max((int) (c.getGreen() * factor), 0),
                    Math.max((int) (c.getBlue() * factor), 0),
                    c.getAlpha());
        }
    }

}

