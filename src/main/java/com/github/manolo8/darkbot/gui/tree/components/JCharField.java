package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Objects;

public class JCharField extends JButton implements OptionEditor {

    private static final char EMPTY = (char) 0;

    private ConfigField field;

    public JCharField() {
        setMargin(new Insets(0, 5, 0, 5));
        setHorizontalAlignment(CENTER);
        putClientProperty("JComponent.minimumWidth", 18);

        addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE || e.getKeyChar() == KeyEvent.VK_DELETE) setValue(EMPTY);
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
        setText((Character) field.get());
        this.field = field;
    }

    public void setText(Character ch) {
        setText(getDisplay(ch));
    }

    public static String getDisplay(Character ch) {
        if (ch == null || ch == EMPTY) return "(unset)";
        return KeyEvent.getKeyText(ch);
    }

    protected void setValue(Character value) {
        setText(value);
        if (field != null) field.set(value);
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

}
