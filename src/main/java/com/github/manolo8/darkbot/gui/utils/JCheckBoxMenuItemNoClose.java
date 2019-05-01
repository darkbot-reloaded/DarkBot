package com.github.manolo8.darkbot.gui.utils;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class JCheckBoxMenuItemNoClose extends JCheckBoxMenuItem {

    public JCheckBoxMenuItemNoClose(String text, Consumer<Boolean> consumer) {
        super(text);
        addActionListener(a -> consumer.accept(isSelected()));
    }

    @Override
    protected void processMouseEvent(MouseEvent evt) {
        if (evt.getID() == MouseEvent.MOUSE_RELEASED && contains(evt.getPoint())) {
            doClick();
            setArmed(true);
        } else {
            super.processMouseEvent(evt);
        }
    }
}
