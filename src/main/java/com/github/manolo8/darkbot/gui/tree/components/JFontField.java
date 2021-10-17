package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;

import javax.swing.*;
import java.awt.*;

public class JFontField extends JTextField implements OptionEditor {

    private ConfigField field;

    public JFontField() {
        this.setColumns(10);
        this.getDocument().addDocumentListener((GeneralDocumentListener) e ->  {
            if (field == null) return;
            Font f = getValue();
            if (f == null) return;
            field.set(f);
            setFont(f.deriveFont(getFont().getSize2D()));
        });
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = null;
        Font font = field.get();
        setText(font.getFontName());
        setFont(font.deriveFont(getFont().getSize2D()));
        this.field = field;
    }

    public Font getValue() {
        Font f = field.get();
        try {
            return new Font(getText(), f.getStyle(), f.getSize());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}
