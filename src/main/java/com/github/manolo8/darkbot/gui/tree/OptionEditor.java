package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.tree.ConfigField;

import javax.swing.*;
import java.awt.*;

public interface OptionEditor {

    JComponent getComponent();
    void edit(ConfigField field);

    default Dimension getReservedSize() {
        return null;
    }

}
