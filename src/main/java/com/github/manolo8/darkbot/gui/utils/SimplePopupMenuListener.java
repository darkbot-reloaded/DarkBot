package com.github.manolo8.darkbot.gui.utils;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public interface SimplePopupMenuListener extends PopupMenuListener {

    @Override
    default void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

    @Override
    default void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

    @Override
    default void popupMenuCanceled(PopupMenuEvent e) {}
}
