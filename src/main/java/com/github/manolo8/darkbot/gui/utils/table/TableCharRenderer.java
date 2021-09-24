package com.github.manolo8.darkbot.gui.utils.table;

import com.github.manolo8.darkbot.gui.tree.editors.CharacterEditor;

import java.awt.*;

public class TableCharRenderer extends TableDelegateRenderer<CharacterEditor> {

    public TableCharRenderer() {
        super(new CharacterEditor(false));
    }

    @Override
    protected void setValue(Object value) {
        delegate.setValue((Character) value);
    }

    private static class RenderCharField extends CharacterEditor {
        /**
         * No-op methods improve performance when using this as a cell renderer, and they are not needed anyways.
         */
        public void validate() {}
        public void invalidate() {}
        public void revalidate() {}
        public void repaint(long tm, int x, int y, int width, int height) {}
        public void repaint(Rectangle r) {}
        public void repaint() {}
        public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}
        public void firePropertyChange(String propertyName, char oldValue, char newValue) {}
        public void firePropertyChange(String propertyName, short oldValue, short newValue) {}
        public void firePropertyChange(String propertyName, int oldValue, int newValue) {}
        public void firePropertyChange(String propertyName, long oldValue, long newValue) {}
        public void firePropertyChange(String propertyName, float oldValue, float newValue) {}
        public void firePropertyChange(String propertyName, double oldValue, double newValue) {}
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
        public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
    }

}
