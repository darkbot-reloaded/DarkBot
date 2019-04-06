package com.github.manolo8.darkbot.config.types.suppliers;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.List;

public abstract class OptionList<T> implements ComboBoxModel<String> {
    public abstract T getValue(String text);
    public abstract String getText(T value);
    public abstract List<String> getOptions();

    private Object selectedItem;

    public void setSelectedItem(Object item) {
        selectedItem = item;
    }
    public Object getSelectedItem() {
        return selectedItem;
    }

    public int getSize() {
        return getOptions().size();
    }

    public String getElementAt(int index) {
        return getOptions().get(index);
    }

    public void addListDataListener(ListDataListener l){}
    public void removeListDataListener(ListDataListener l){}
}
