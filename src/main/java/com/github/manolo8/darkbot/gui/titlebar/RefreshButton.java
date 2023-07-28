package com.github.manolo8.darkbot.gui.titlebar;

import com.formdev.flatlaf.ui.FlatButtonUI;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.utils.ForwardAnimatedIcon;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
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
            ForwardAnimatedIcon.toggleState(this);
            System.out.println("Triggering refresh: user requested");
            Main.API.handleRefresh();
        });
    }

    private static class RefreshIcon extends ForwardAnimatedIcon {
        private Path2D refreshPath;

        public RefreshIcon() {
            super(16, 16, null);
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y, float animatedValue) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(getColor(c));

            // svg path converted into java2d
            // https://github.com/JFormDesigner/FlatLaf/blob/main/flatlaf-demo/src/main/resources/com/formdev/flatlaf/demo/icons/refresh.svg
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

            g2.rotate(animatedValue * Math.PI, getIconWidth() / 2.0, getIconHeight() / 2.0);
            g2.fill(refreshPath);
        }

        @Override
        public int getAnimationDuration() {
            return 250;
        }

        private Color getColor(Component c) {
            Color foreground = c.getForeground();
            return FlatButtonUI.buttonStateColor(c, foreground, null, null,
                    UIUtils.darker(foreground, 0.75), UIUtils.darker(foreground, 0.6));
        }
    }

}

