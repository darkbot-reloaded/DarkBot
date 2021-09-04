package com.github.manolo8.darkbot.gui.utils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import java.awt.*;
import java.util.function.Consumer;

public class SearchField extends JTextField implements GeneralDocumentListener {

    private static final Icon SEARCH_ICON = UIUtils.getIcon("search");
    private static final Border MARGIN_BORDER = new EmptyBorder(0, SEARCH_ICON.getIconWidth(), 0, 0);
    private final Consumer<String> onChange;

    public SearchField() {
        this(null);
    }

    public SearchField(Consumer<String> onChange) {
        this.onChange = onChange;
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (propertyName.equals("document") && oldValue != newValue) {
            if (oldValue != null) ((Document) oldValue).removeDocumentListener(this);
            if (newValue != null) ((Document) newValue).addDocumentListener(this);
        }
        super.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    public void update(DocumentEvent e) {
        if (onChange != null) onChange.accept(getText());
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
