package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.types.Col;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

public class JColorField extends JTextField implements OptionEditor {

    private ConfigField field;
    private Col col;
    private final Color DARK_GRAY = new java.awt.Color(0x222222), LIGHT_GRAY = new java.awt.Color(0xDDDDDD);

    public JColorField() {
        this.setColumns(8);
        this.getDocument().addDocumentListener((GeneralDocumentListener) e ->  {
            if (field == null) return;
            Color c = getValue();
            if (c == null) return;
            field.set(c);
            setColor(c);
        });
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = null;
        Color color = field.get();
        col = field.field.getAnnotation(Col.class);

        int rgba = color.getRGB();
        if (!hasAlpha()) setText(String.format("#%06X", rgba & 0x00FFFFFF));
        else setText(String.format("#%08X", rgba));

        setColor(color);
        this.field = field;
    }

    private void setColor(Color color) {
        setBackground(color); // Remove alpha
        setForeground(getBrightness(color) > 128 ? DARK_GRAY : LIGHT_GRAY);
    }

    private boolean hasAlpha() {
        return col == null || col.alpha();
    }

    public Color getValue() {
        try {
            return new Color(Integer.parseUnsignedInt(getText().replace("#", ""), 16), hasAlpha());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // http://alienryderflex.com/hsp.html
    private int getBrightness(Color color) {
        color = UIUtils.blendColor(color, -1);
        return (int) Math.sqrt(
                color.getRed() * color.getRed() * 0.299d +
                        color.getGreen() * color.getGreen() * 0.587d +
                        color.getBlue() * color.getBlue() * 0.114d);
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}
