package com.github.manolo8.darkbot.core.itf;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.utils.JTitledPopupMenuSeparator;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.stream.Stream;

public interface ExtraMenuProvider {
    Collection<JComponent> getExtraMenuItems(Main main);

    /**
     * If the extra menu items should automatically be put in a submenu for the plugin
     * @return true if they should be hidden in a submenu, false otherwise
     */
    default boolean autoSubmenu() {
        return false;
    }

    /**
     * Utility method to create a menu item
     * @param key The translation key, or name of the item
     * @param listener The action to take when pressed
     * @return The created JMenuItem with the action listener
     */
    default JMenuItem create(String key, ActionListener listener) {
        JMenuItem item = new JMenuItem(I18n.getOrDefault("gui.hamburger_button." + key, key));
        if (listener != null) item.addActionListener(listener);
        return item;
    }

    default JMenuItem create(String key, Icon icon, ActionListener listener) {
        JMenuItem item = new JMenuItem(I18n.getOrDefault("gui.hamburger_button." + key, key), icon);
        if (listener != null) item.addActionListener(listener);
        return item;
    }

    default JMenu createMenu(String key, Stream<JComponent> components) {
        JMenu menu = new JMenu(key);
        components.forEach(menu::add);
        return menu;
    }

    default JComponent createSeparator(String key) {
        return new JTitledPopupMenuSeparator(I18n.getOrDefault("gui.hamburger_button." + key, key));
    }

}
