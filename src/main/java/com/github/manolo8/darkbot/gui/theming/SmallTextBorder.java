package com.github.manolo8.darkbot.gui.theming;

import com.bulenkov.darcula.ui.DarculaTextBorder;
import com.bulenkov.darcula.ui.DarculaTextFieldUI;

import javax.swing.*;
import javax.swing.plaf.InsetsUIResource;
import java.awt.*;

public class SmallTextBorder extends DarculaTextBorder {

    @Override
    public Insets getBorderInsets(Component c) {
        return c instanceof JComponent && Boolean.TRUE.equals(((JComponent) c).getClientProperty("ConfigTree")) ?
                new InsetsUIResource(2, 3, 2, 3) : super.getBorderInsets(c);
    }

}
