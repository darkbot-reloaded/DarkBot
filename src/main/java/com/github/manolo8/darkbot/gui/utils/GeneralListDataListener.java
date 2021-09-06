package com.github.manolo8.darkbot.gui.utils;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public interface GeneralListDataListener extends ListDataListener {
    @Override
    default void intervalAdded(ListDataEvent e) {}

    @Override
    default void intervalRemoved(ListDataEvent e) {}

    @Override
    default void contentsChanged(ListDataEvent e) {}
}
