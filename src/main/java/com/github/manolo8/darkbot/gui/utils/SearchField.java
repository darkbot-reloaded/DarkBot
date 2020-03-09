package com.github.manolo8.darkbot.gui.utils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class SearchField extends JTextField {

    private static final Icon SEARCH_ICON = UIUtils.getIcon("search");
    private static final Border MARGIN_BORDER = new EmptyBorder(0, SEARCH_ICON.getIconWidth() + 6, 0, 6);

    public SearchField(Consumer<String> filterChange) {
        getDocument().addDocumentListener((GeneralDocumentListener) e -> filterChange.accept(getText()));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        int margin = (getHeight() - SEARCH_ICON.getIconHeight()) / 2;
        SEARCH_ICON.paintIcon(this, graphics, margin, margin);
    }

    @Override
    public void setBorder(Border border) {
        super.setBorder(new CompoundBorder(border, MARGIN_BORDER));
    }

}
