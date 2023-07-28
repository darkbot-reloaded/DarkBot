package com.github.manolo8.darkbot.gui.utils;

import com.formdev.flatlaf.icons.FlatAnimatedIcon;

import javax.swing.*;
import java.awt.*;

public abstract class ForwardAnimatedIcon extends FlatAnimatedIcon {
    private static final String BUTTON_STATE = "ANIMATION_BUTTON_STATE";

    private boolean flip;

    public static void toggleState(JComponent c) {
        Boolean state = (Boolean) c.getClientProperty(ForwardAnimatedIcon.BUTTON_STATE);
        c.putClientProperty(ForwardAnimatedIcon.BUTTON_STATE,
                Boolean.TRUE.equals(state) ? Boolean.FALSE : Boolean.TRUE);
    }

    public ForwardAnimatedIcon(int width, int height, Color color) {
        super(width, height, color);
    }

    public abstract void paintIcon(Component c, Graphics g, int x, int y, float animatedValue);

    @Override
    public void paintIconAnimated(Component c, Graphics g, int x, int y, float animatedValue) {
        animatedValue = flip ? 1 - animatedValue : animatedValue;
        paintIcon(c, g, x, y, animatedValue);
    }

    @Override
    public float getValue(Component c) {
        JComponent component = (JComponent) c;
        Boolean state = (Boolean) component.getClientProperty(BUTTON_STATE);
        return (flip = Boolean.TRUE.equals(state)) ? 1 : 0;
    }
}
