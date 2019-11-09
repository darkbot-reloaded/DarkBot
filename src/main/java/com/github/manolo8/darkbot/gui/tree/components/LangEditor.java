package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.utils.I18n;

import java.awt.event.ActionEvent;

public class LangEditor extends JListField {
    @Override
    protected void updateValue(ActionEvent event) {
        super.updateValue(event);
        I18n.reloadProps();
    }
}
