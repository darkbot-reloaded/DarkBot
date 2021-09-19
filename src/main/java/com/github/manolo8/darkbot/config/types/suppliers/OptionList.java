package com.github.manolo8.darkbot.config.types.suppliers;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Deprecated
public abstract class OptionList<T> implements ComboBoxModel<String> {
    protected EventListenerList dataListeners = new EventListenerList();

    public abstract T getValue(String text);
    public abstract String getText(T value);
    public String getTooltip(String text) {
        return getTooltipFromVal(getValue(text));
    }
    public String getTooltipFromVal(T value) {
        return null;
    }
    public String getShortText(T value) {
        return getText(value);
    }
    public abstract List<String> getOptions();

    private Object selectedItem;

    public void setSelectedItem(Object item) {
        selectedItem = item;
    }
    public Object getSelectedItem() {
        return selectedItem;
    }

    public int getSize() {
        List<String> options = getOptions();
        return options == null ? 0 : options.size();
    }

    public String getElementAt(int index) {
        return getOptions().get(index);
    }

    public void addListDataListener(ListDataListener l) {
        dataListeners.add(ListDataListener.class, l);
    }
    public void removeListDataListener(ListDataListener l) {
        dataListeners.remove(ListDataListener.class, l);
    }

    public static void forceUpdate(Collection<? extends OptionList<?>> instances, int size) {
        SwingUtilities.invokeLater(() -> instances.forEach(model -> {
            ListDataEvent ev = new ListDataEvent(model, ListDataEvent.CONTENTS_CHANGED, 0, size);
            Arrays.stream(model.dataListeners.getListeners(ListDataListener.class))
                    .forEach(listener -> listener.contentsChanged(ev));
        }));
    }
}
