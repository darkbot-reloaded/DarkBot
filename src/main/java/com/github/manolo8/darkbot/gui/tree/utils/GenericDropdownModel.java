package com.github.manolo8.darkbot.gui.tree.utils;

import eu.darkbot.api.config.annotations.Dropdown;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GenericDropdownModel<E> extends AbstractListModel<E> implements ComboBoxModel<E> {

    private final Dropdown.Options<E> options;
    private final List<E> list = new ArrayList<>();

    private Object selected;

    public GenericDropdownModel(Dropdown.Options<E> options) {
        this.options = options;
        checkUpdates();
    }

    public void checkUpdates() {
        List<E> newList = options.options();
        if (newList == null || list.equals(newList)) return;

        list.clear();
        list.addAll(newList);
        fireContentsChanged(this, 0, getSize() - 1);
    }

    @Override
    public void setSelectedItem(Object selected) {
        this.selected = selected;
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public E getElementAt(int index) {
        return list.get(index);
    }
}
