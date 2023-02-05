package com.github.manolo8.darkbot.gui.components;

import com.formdev.flatlaf.FlatClientProperties;
import com.github.manolo8.darkbot.utils.I18n;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class TabbedPane extends JPanel {

    private Tab current;

    @Getter
    private final List<JComponent> header = new ArrayList<>();

    public TabbedPane() {
        super(new BorderLayout());
    }

    public void addTab(Icon icon, String key, JComponent component) {
        addTab(new Tab(icon, key, component), true);
    }

    public AbstractButton addHiddenTab(Icon icon, String name, JComponent component) {
        return addTab(new Tab(icon, name, component), false);
    }

    private Tab addTab(Tab tab, boolean inHeader) {
        if (inHeader) header.add(tab);
        if (current == null) selectTab(tab);
        return tab;
    }

    private void selectTab(Tab tab) {
        Tab old = current;
        current = tab;
        if (old != null) {
            old.setSelected(false);
            remove(old.component);
        }
        current.setSelected(true);
        add(tab.component, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private class Tab extends JToggleButton implements ActionListener {
        private final JComponent component;

        private Tab(Icon icon, String key, JComponent component) {
            super(I18n.getOrDefault(key, null), icon);

            String description = key == null ? null : I18n.getOrDefault(key + ".desc", null);
            if (description != null) setToolTipText(description);

            this.component = component;

            addActionListener(this);
            setMargin(new Insets(0, 8, 0, 8));
            putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TAB);
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension max = super.getMaximumSize();
            max.height = Short.MAX_VALUE;
            return max;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            selectTab(this);
        }

    }

}
