package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;

public class LangEditor extends JListField {
    @Override
    protected void updateValue() {
        super.updateValue();
        I18n.reloadProps();
        Popups.showMessageAsync(
                I18n.get("language.changed.title"),
                I18n.get("language.changed.content", I18n.getLocale().getDisplayName(I18n.getLocale()), I18n.get("translation.credit")), JOptionPane.INFORMATION_MESSAGE);
    }
}
