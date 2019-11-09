package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.utils.I18n;

public class LangEditor extends JListField {
    @Override
    protected void updateValue() {
        super.updateValue();
        I18n.reloadProps();
    }
}
