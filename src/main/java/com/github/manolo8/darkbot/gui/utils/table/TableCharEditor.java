package com.github.manolo8.darkbot.gui.utils.table;

import com.github.manolo8.darkbot.gui.tree.editors.CharacterEditor;

import javax.swing.*;

public class TableCharEditor extends TableDelegateEditor<CharacterEditor> {

    public TableCharEditor() {
        super(new CharacterEditor(false));
    }

    @Override
    protected void setValue(Object value) {
        delegate.setValue((Character) value);
        SwingUtilities.invokeLater(delegate::requestFocus);
    }

    @Override
    protected Object getValue() {
        return delegate.getEditorValue();
    }

}
