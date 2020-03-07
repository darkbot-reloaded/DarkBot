package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class JLabelField extends javax.swing.JLabel implements OptionEditor {

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        setText(Objects.toString(field.get(), ""));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = AdvancedConfig.ROW_HEIGHT;
        return d;
    }

    /**
     * No-op methods improve performance when using this as a cell renderer, and they are not needed anyways.
     */
    public void validate() {}
    public void invalidate() {}
    public void revalidate() {}
    public void repaint(long tm, int x, int y, int width, int height) {}
    public void repaint(Rectangle r) {}
    public void repaint() {}
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {}
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {}
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {}
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {}
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {}
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {}
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
}
