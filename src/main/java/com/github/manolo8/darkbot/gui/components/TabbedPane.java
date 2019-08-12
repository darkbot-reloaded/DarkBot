package com.github.manolo8.darkbot.gui.components;

import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;

public class TabbedPane extends JPanel {

    private Tab current;

    private JPanel header = new JPanel(new MigLayout("ins 0, gap 0"));

    public JPanel getHeader() {
        return header;
    }

    public TabbedPane() {
        super(new MigLayout("ins 0, gap 0, fill", "[grow]", "[grow]"));
        setBorder(UIUtils.getPartialBorder(true));
    }

    public void addTab(Icon icon, String name, JComponent component) {
        addTab(new Tab(icon, name, component), true);
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
            remove(old.component);
            old.setBackground();
        }
        add(tab.component, "grow");
        tab.setBackground();
        revalidate();
        repaint();
    }

    private static final Border UNSELECTED = UIUtils.getBorder(),
            SELECTED = UIUtils.getPartialBorder(false);

    private class Tab extends MainButton {

        private final JComponent component;

        private Tab(Icon icon, String name, JComponent component) {
            super(icon, name);
            this.component = component;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            selectTab(this);
        }

        protected void setBackground() {
            if (current == this) {
                //super.setBackground();
                setBorder(SELECTED);
            } else {
                //setBackground(actionColor.darker());
                setBorder(UNSELECTED);
            }
            super.setBackground();
        }

    }

}
