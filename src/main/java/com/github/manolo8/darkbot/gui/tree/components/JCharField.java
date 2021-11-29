package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Objects;

public class JCharField extends JButton implements OptionEditor {

    private static final char EMPTY = (char) 0;

    private ConfigField field;
    private Character value;

    public JCharField() {
        setModel(new StaticButtonModel());

        setMargin(new Insets(0, 5, 0, 5));
        setHorizontalAlignment(CENTER);
        putClientProperty("JComponent.minimumWidth", 18);

        addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE || e.getKeyChar() == KeyEvent.VK_DELETE) setValue(null);
                else setValue((char) e.getKeyCode());
                e.consume();
            }

            @Override public void keyTyped(KeyEvent e) {}
            @Override public void keyReleased(KeyEvent e) {}
        });
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = null;
        setValue(field.get());
        this.field = field;
    }

    public void setValue(Character value) {
        setText(getDisplay(this.value = value));
        if (field != null) field.set(value);
    }

    public Character getValue() {
        return value;
    }

    public static String getDisplay(Character ch) {
        if (ch == null || ch == EMPTY) return "";
        return KeyEvent.getKeyText(ch);
    }

    @Override
    public boolean isDefaultButton() {
        return false;
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

    @Override
    public Dimension getReservedSize() {
        return new Dimension(140, 0);
    }


    private static final Icon KEY_ICON = UIUtils.getIcon("keybind");

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        int margin = (getHeight() - KEY_ICON.getIconHeight()) / 2;
        KEY_ICON.paintIcon(this, graphics, getWidth() - KEY_ICON.getIconWidth(), margin);
    }

    /**
     * Add an extra border to fit the key icon
     */
    public static class ExtraBorder extends JCharField {
        private static final Border MARGIN_BORDER = new EmptyBorder(0, 0, 0, KEY_ICON.getIconWidth() / 2);

        @Override
        public void setBorder(Border border) {
            super.setBorder(new CompoundBorder(border, MARGIN_BORDER));
        }

    }

    private static class StaticButtonModel extends DefaultButtonModel {
        @Override
        public void setArmed(boolean b) {}

        @Override
        public void setPressed(boolean b) {}

        @Override
        public void setRollover(boolean b) {}
    }

}
