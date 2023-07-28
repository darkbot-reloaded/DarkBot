package com.github.manolo8.darkbot.gui.utils;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.ui.FlatBorder;
import com.github.manolo8.darkbot.config.ConfigEntity;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.net.URL;
import java.util.Objects;

public class UIUtils {

    public static final Color GREEN = Color.decode("#3D6E3D"),
            YELLOW = Color.decode("#6E6E28"),
            RED = Color.decode("#6E2B28"),
            RED_HIGHLIGHT = new Color(0x40FF1300, true),
            GREEN_HIGHLIGHT = new Color(0x1A00FF00, true),
            BACKGROUND = UIManager.getColor("Viewport.background"), // Normal background of things
            BORDER = UIManager.getColor("Component.borderColor"), // Normal border of things
            TAB_HIGLIGHT = UIManager.getColor("TabbedPane.underlineColor");

    public static FlatSVGIcon getSVGIcon(String name, int width, int height) {
        URL url = UIUtils.class.getResource("/" + name + ".svg");
        if (url != null) return getSvgIcon(url, width, height);

        url = Objects.requireNonNull(UIUtils.class.getResource("/missing.svg"));
        return getSvgIcon(url, width, height);
    }

    public static Icon getIcon(String name) {
        // Prefer SVG if available
        URL url = UIUtils.class.getResource("/" + name + ".svg");
        if (url != null) return getSvgIcon(url, 16, 16);

        // Fallback to png
        url = UIUtils.class.getResource("/" + name + ".png");
        if (url != null) return getPngIcon(url);

        System.err.println("Failed to locate icon: '" + name + "'");
        url = Objects.requireNonNull(UIUtils.class.getResource("/missing.svg"));
        return getSvgIcon(url, 16, 16);
    }

    private static FlatSVGIcon getSvgIcon(URL url, int width, int height) {
        return new FlatSVGIcon(url).derive(width, height);
    }

    private static Icon getPngIcon(URL url) {
        return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
    }

    public static Image getImage(String name) {
        return new ImageIcon(Objects.requireNonNull(UIUtils.class.getResource("/" + name + ".png"))).getImage();
    }

    public static Insets getInsetConfig(boolean textPadding) {
        int inset = ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.BOT_GUI.BUTTON_SIZE;
        return new Insets(inset, inset + (textPadding ? 4 : 0), inset, inset + (textPadding ? 4 : 0));
    }

    public static Border getBorderWithInsets(boolean textPadding) {
        return new FlatBorder() {
            @Override
            public Insets getBorderInsets(Component c, Insets insets) {
                return getInsetConfig(textPadding);
            }
        };
    }

    public static Border getBorder() {
        return new FlatBorder();
    }

    public static Color blendColor(Color color, int alpha) {
        float factor = (alpha != -1 ? alpha : color.getAlpha()) / 255f;
        int red = (int) (BACKGROUND.getRed() * (1 - factor) + color.getRed() * factor);
        int green = (int) (BACKGROUND.getGreen() * (1 - factor) + color.getGreen() * factor);
        int blue = (int) (BACKGROUND.getBlue() * (1 - factor) + color.getBlue() * factor);
        return new Color(red, green, blue);
    }

    public static Border getPartialBorder(int top, int left, int bottom, int right) {
        return new MatteBorder(top, left, bottom, right, BORDER);
    }

    public static JLabel setRed(JLabel label, boolean set) {
        Font baseFont = label.getFont();
        if (set) {
            label.setForeground(UIUtils.RED.brighter().brighter());
            label.setFont(baseFont.deriveFont(baseFont.getStyle() | Font.BOLD));
        } else {
            label.setForeground(null);
            label.setFont(baseFont.deriveFont(baseFont.getStyle() & ~Font.BOLD));
        }
        return label;
    }

    public static Color darker(Color c, double factor) {
        return new Color(Math.max((int) (c.getRed() * factor), 0),
                Math.max((int) (c.getGreen() * factor), 0),
                Math.max((int) (c.getBlue() * factor), 0),
                c.getAlpha());
    }

    public static Color getTrafficLight(double value, double maximum) {
        double hue = 1 - Math.min(maximum, Math.max(0, value)) / maximum;
        return Color.getHSBColor((float) (hue * 0.35), 0.7f, 0.55f);
    }
}
