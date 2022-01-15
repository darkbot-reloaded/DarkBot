package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.OptionEditor;
import eu.darkbot.api.utils.Inject;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class CharacterEditor extends JButton implements OptionEditor<Character>, KeyListener {

    private static final Icon KEY_ICON = UIUtils.getIcon("keybind");
    private static final Border MARGIN_BORDER = new EmptyBorder(0, 0, 0, KEY_ICON.getIconWidth() / 2);
    private static final char EMPTY = (char) 0;

    private final boolean marginBorder;

    private Character value;


    @Inject
    public CharacterEditor() {
        this(true);
    }

    public CharacterEditor(boolean marginBorder) {
        setModel(new StaticButtonModel());

        setMargin(new Insets(0, 5, 0, 5));
        setHorizontalAlignment(CENTER);
        putClientProperty("JComponent.minimumWidth", 18);

        addKeyListener(this);

        this.marginBorder = marginBorder;
        if (marginBorder) setBorder(getBorder());
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<Character> character) {
        setValue(character.getValue());
        return this;
    }

    @Override
    public Character getEditorValue() {
        return value;
    }

    public void setValue(Character value) {
        setText(getDisplay(this.value = value));
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

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        int margin = (getHeight() - KEY_ICON.getIconHeight()) / 2;
        KEY_ICON.paintIcon(this, graphics, getWidth() - KEY_ICON.getIconWidth(), margin);
    }

    @Override
    public void setBorder(Border border) {
        if (marginBorder) super.setBorder(new CompoundBorder(border, MARGIN_BORDER));
        else super.setBorder(border);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Let enter & escape be handled by parents, or space if this isn't the target
        char key = e.getKeyChar();
        if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_SHIFT ||
                (e.getComponent() != this && e.getKeyChar() == KeyEvent.VK_SPACE)) return;
        // Backspace or delete clears the key, any other value is saved, and the event is consumed
        if (key == KeyEvent.VK_BACK_SPACE || key == KeyEvent.VK_DELETE) setValue(null);
        else setValue((char) e.getKeyCode());
        e.consume();
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    private static class StaticButtonModel extends DefaultButtonModel {
        @Override
        public void setArmed(boolean b) {}

        @Override
        public void setPressed(boolean b) {}

        @Override
        public void setRollover(boolean b) {}
    }

}

