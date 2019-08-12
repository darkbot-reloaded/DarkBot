package com.github.manolo8.darkbot.gui.utils;

import com.bulenkov.iconloader.util.Gray;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.gui.titlebar.CloseButton;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class UIUtils {

    public static final Color GREEN = Color.decode("#3D6E3D"),
            YELLOW = Color.decode("#6E6E28"),
            RED = Color.decode("#6E2B28");

    public static ImageIcon getIcon(String name) {
        return new ImageIcon(new ImageIcon(UIUtils.class.getResource("/" + name + ".png")).getImage()
                .getScaledInstance(16, 16, Image.SCALE_SMOOTH));
    }

    public static Image getImage(String name) {
        return new ImageIcon(UIUtils.class.getResource("/" + name + ".png")).getImage();
    }

    public static Insets getInsetConfig(boolean textPadding) {
        int inset = ConfigEntity.INSTANCE.getConfig().MISCELLANEOUS.DISPLAY.BUTTON_SIZE;
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

    public static Border getPartialBorder(boolean connectTop) {
        return new MatteBorder(connectTop ? 0 : 1, 1, connectTop ? 1 : 0, 1, Gray._100);
    }

}
