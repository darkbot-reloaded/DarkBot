package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.OptionEditor;
import eu.darkbot.api.config.util.ValueHandler;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

public class ColorEditor extends JTextField implements OptionEditor<Color>, GeneralDocumentListener {

    private Color color;
    private boolean alpha;
    private boolean valid;

    private final Color DARK_GRAY = new java.awt.Color(0x222222), LIGHT_GRAY = new java.awt.Color(0xDDDDDD);

    public ColorEditor() {
        this.setColumns(8);
        this.getDocument().addDocumentListener(this);
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<Color> color) {
        this.alpha = Boolean.TRUE.equals(color.getMetadata("alpha"));
        this.color = color.getValue();

        int rgba = this.color.getRGB();
        if (alpha) setText(String.format("#%08X", rgba));
        else setText(String.format("#%06X", rgba & 0x00FFFFFF));

        setEnabled(!Boolean.TRUE.equals(color.getMetadata("readonly")));

        return this;
    }

    @Override
    public Color getEditorValue() {
        return color;
    }

    public void update(DocumentEvent e) {
        Color newColor = parseColor(getText());
        setValid(newColor != null);
        if (newColor == null) return; // Invalid color, no update

        this.color = newColor;
        setBackground(color); // Remove alpha
        setForeground(getBrightness(color) > 128 ? DARK_GRAY : LIGHT_GRAY);
    }

    private void setValid(boolean valid) {
        if (this.valid == valid) return; // No change
        this.valid = valid;
        putClientProperty("JComponent.outline", valid ? null : "error");
    }

    private Color parseColor(String text) {
        if (text == null || !text.startsWith("#")) return null;
        int len = text.length();
        if (len != (alpha ? 9 : 7)) return null;
        int val;
        try {
            val = Integer.parseUnsignedInt(getText().replace("#", ""), 16);
        } catch (NumberFormatException e) {
            return null;
        }
        return new Color(val, alpha);
    }

    @Override
    public boolean stopCellEditing() {
        return valid;
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
