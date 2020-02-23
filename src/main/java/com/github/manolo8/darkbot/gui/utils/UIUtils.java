package com.github.manolo8.darkbot.gui.utils;

import com.bulenkov.iconloader.util.Gray;
import com.github.manolo8.darkbot.config.ConfigEntity;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class UIUtils {

    public static final Color GREEN = Color.decode("#3D6E3D"),
            YELLOW = Color.decode("#6E6E28"),
            RED = Color.decode("#6E2B28"),
            BACKGROUND = new Color(60, 63, 65), // Normal background of things
            ACTION = new Color(50, 53, 55), // When hovering or clicking a btn
            SELECTED_BLUE = new Color(74, 136, 199);

    public static ImageIcon getIcon(String name) {
        return new ImageIcon(new ImageIcon(UIUtils.class.getResource("/" + name + ".png")).getImage()
                .getScaledInstance(16, 16, Image.SCALE_SMOOTH));
    }

    public static Image getImage(String name) {
        return new ImageIcon(UIUtils.class.getResource("/" + name + ".png")).getImage();
    }

    public static Insets getInsetConfig(boolean textPadding) {
        int inset = ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.DISPLAY.BUTTON_SIZE;
        return new Insets(inset, inset + (textPadding ? 4 : 0), inset, inset + (textPadding ? 4 : 0));
    }

    public static Border getBorderWithInsets(boolean textPadding) {
        return new LineBorder(Gray._100) {
            @Override
            public Insets getBorderInsets(Component c, Insets insets) {
                return getInsetConfig(textPadding);
            }
        };
    }

    public static Border getBorder() {
        return BorderFactory.createLineBorder(Gray._100);
    }

    public static Color blendColor(Color color, int alpha) {
        float factor = (alpha != -1 ? alpha : color.getAlpha()) / 255f;
        int red = (int) (BACKGROUND.getRed() * (1 - factor) + color.getRed() * factor);
        int green = (int) (BACKGROUND.getGreen() * (1 - factor) + color.getGreen() * factor);
        int blue = (int) (BACKGROUND.getBlue() * (1 - factor) + color.getBlue() * factor);
        return new Color(red, green, blue);
    }

    public static Border getPartialBorder(int top, int left, int bottom, int right) {
        return new MatteBorder(top, left, bottom, right, Gray._100);
    }

    public static Border getTabBorder(boolean selected) {
        return new CompoundBorder(getPartialBorder(1, 0, 0, 0), new MatteBorder(0, 0, 2, 0, selected ? SELECTED_BLUE : Gray._128));
    }

}
