package com.github.manolo8.darkbot.gui.tree.utils;

import eu.darkbot.api.config.annotations.Dropdown;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class GenericDropdownModel<E> extends AbstractListModel<E> implements ComboBoxModel<E> {

    private final Dropdown.Options<E> options;
    private final List<E> list = new ArrayList<>();

    private Object selected;

    public GenericDropdownModel(Dropdown.Options<E> options) {
        this.options = options;
        checkUpdates();
    }

    public void checkUpdates() {
        Collection<E> newList = options.options();
        if (newList == null || isUnchanged(newList)) return;

        list.clear();
        list.addAll(newList);
        fireContentsChanged(this, 0, getSize() - 1);
    }

    private boolean isUnchanged(Collection<E> newList) {
        Iterator<E> it1 = list.iterator(), it2 = newList.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            E obj1 = it1.next(), obj2 = it2.next();
            if (!(Objects.equals(obj1, obj2))) return false;
        }
        return !(it1.hasNext() || it2.hasNext());
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
