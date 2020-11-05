package com.github.manolo8.darkbot.gui.components;

import com.formdev.flatlaf.ui.FlatButtonBorder;
import com.github.manolo8.darkbot.gui.utils.CustomTabBorder;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class TabbedPane extends JPanel {

    private Tab current;

    private List<JComponent> header = new ArrayList<>();

    public List<JComponent> getHeader() {
        return header;
    }

    public TabbedPane() {
        super(new MigLayout("ins 0, gap 0, fill", "[grow]", "[grow]"));
        setBorder(UIUtils.getPartialBorder(0, 1, 1, 1));
    }

    public void addTab(Icon icon, String key, JComponent component) {
        addTab(new Tab(icon, key, component), true);
    }

    public MainButton addHiddenTab(Icon icon, String name, JComponent component) {
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
            old.updateBorder();
            remove(old.component);
        }
        current.updateBorder();
        add(tab.component, "grow");
        revalidate();
        repaint();
    }

    private class Tab extends MainButton {
        private final Border HIGHLIGHT = new CustomTabBorder(0, 0, 2, 0), DEFAULT = new FlatButtonBorder();

        private final JComponent component;

        private Tab(Icon icon, String key, JComponent component) {
            super(icon, I18n.getOrDefault(key, null),
                    key == null ? null : I18n.getOrDefault(key + ".desc", null));
            setFocusPainted(false);

            this.component = component;
        }

        public void updateBorder() {
            setBorder(current == this ? HIGHLIGHT : DEFAULT);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            selectTab(this);
        }

    }

}
