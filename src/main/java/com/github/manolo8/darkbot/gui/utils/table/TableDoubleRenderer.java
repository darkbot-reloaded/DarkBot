package com.github.manolo8.darkbot.gui.utils.table;

import javax.swing.*;
import java.awt.*;

public class TableDoubleRenderer extends TableDelegateRenderer<JSpinner> {

    public TableDoubleRenderer() {
        super(new RenderSpinner());
    }

    @Override
    protected void setValue(Object value) {
        delegate.setValue(value);
    }

    private static class RenderSpinner extends JSpinner {

        @Override
        public Insets getInsets() {
            return new Insets(0, 0, 0, 0);
        }

        /**
         * No-op methods improve performance when using this as a cell renderer, and they are not needed anyways.
         */
        //public void validate() {} // JSpinner needs it to render
        //public void invalidate() {} // JSpinner needs it to resize properly
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
