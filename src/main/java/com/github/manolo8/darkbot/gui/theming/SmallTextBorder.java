package com.github.manolo8.darkbot.gui.theming;

import com.bulenkov.darcula.ui.DarculaTextBorder;
import com.bulenkov.darcula.ui.DarculaTextFieldUI;

import javax.swing.*;
import javax.swing.plaf.InsetsUIResource;
import java.awt.*;

public class SmallTextBorder extends DarculaTextBorder {

    @Override
    public Insets getBorderInsets(Component c) {
        if (c instanceof JComponent && Boolean.TRUE.equals(((JComponent) c).getClientProperty("ConfigTree"))) {
            return DarculaTextFieldUI.isSearchField(c) ? super.getBorderInsets(c) : new InsetsUIResource(2, 3, 2, 3);
        } else return super.getBorderInsets(c);
    }

}
