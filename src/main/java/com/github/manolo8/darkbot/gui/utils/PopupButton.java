package com.github.manolo8.darkbot.gui.utils;

import com.github.manolo8.darkbot.gui.components.MainButton;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 * A button that toggles a popup when clicked
 */
public class PopupButton<T extends JPopupMenu>
        extends MainButton
        implements SimpleMouseListener, SimplePopupMenuListener {

    protected final T popup;
    protected long keepClosed;

    public PopupButton(Icon icon, String text, T popup) {
        super(icon, text);
        this.popup = popup;
        this.popup.addPopupMenuListener(this);
        this.addMouseListener(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (keepClosed > System.currentTimeMillis()) keepClosed = Long.MAX_VALUE;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        keepClosed = 0;
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        keepClosed = System.currentTimeMillis() + 100;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (keepClosed > System.currentTimeMillis()) {
            keepClosed = 0;
            return;
        }
        showPopup();
    }

    protected void showPopup() {
        popup.show(this, 0, getHeight() - 1);
    }

}
