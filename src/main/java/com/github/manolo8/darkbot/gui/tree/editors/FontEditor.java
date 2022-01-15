package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.OptionEditor;
import eu.darkbot.api.config.util.ValueHandler;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

public class FontEditor extends JTextField implements OptionEditor<Font>, GeneralDocumentListener {

    private Font font;
    private boolean valid;

    public FontEditor() {
        this.setColumns(10);
        this.getDocument().addDocumentListener(this);
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<Font> font) {
        this.font = font.getValue();
        setText(this.font.getFontName());
        setEditable(!Boolean.TRUE.equals(font.getMetadata("readonly")));

        return this;
    }

    @Override
    public Font getEditorValue() {
        return font;
    }

    @Override
    public boolean stopCellEditing() {
        return valid;
    }

    @Override
    public void update(DocumentEvent e) {
        Font newFont = parseFont(getText());
        setValid(newFont != null);
        if (newFont == null) return;

        this.font = newFont;
        setFont(font.deriveFont(getFont().getSize2D()));
    }

    private void setValid(boolean valid) {
        if (this.valid == valid) return; // No change
        this.valid = valid;
        putClientProperty("JComponent.outline", valid ? null : "error");
    }

    public Font parseFont(String text) {
        if (text == null || text.isEmpty()) return null;
        Font newFont = new Font(text, font.getStyle(), font.getSize());
        if (!newFont.getFontName().equalsIgnoreCase(text)) return null;
        return newFont;
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}

