package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.tree.ConfigField;

import javax.swing.*;

public interface OptionEditor {

    JComponent getComponent();
    void edit(ConfigField field);

}
