package com.github.manolo8.darkbot.gui.zones.safety;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.SafetyInfo;
import com.github.manolo8.darkbot.utils.I18n;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

class SafetyEditor extends JPanel {

    private JComboBox<SafetyInfo.RunMode> runEditor;
    private JComboBox<SafetyInfo.JumpMode> jumpEditor;
    private JComboBox<SafetyInfo.CbsMode> cbsEditor;

    SafetyEditor(SafetiesEditor editor) {
        super(new MigLayout("wrap 3, fill", "[][][]"));

        runEditor = new JComboBox<>(SafetyInfo.RunMode.values());
        jumpEditor = new JComboBox<>(SafetyInfo.JumpMode.values());
        cbsEditor = new JComboBox<>(SafetyInfo.CbsMode.values());
        edit(null);

        add(new JLabel(I18n.get("safety_places.run_mode")));
        add(new JLabel(I18n.get("safety_places.jump_mode")));
        add(new JLabel(I18n.get("safety_places.cbs_mode")));
        add(runEditor, "grow");
        add(jumpEditor, "grow");
        add(cbsEditor, "grow");

        runEditor.addActionListener(a -> {
            if (editor.editing != null) {
                editor.editing.runMode = runEditor.getItemAt(runEditor.getSelectedIndex());
                ConfigEntity.changed();
            }
        });
        jumpEditor.addActionListener(a -> {
            if (editor.editing != null) {
                editor.editing.jumpMode = jumpEditor.getItemAt(jumpEditor.getSelectedIndex());
                ConfigEntity.changed();
            }
        });
        cbsEditor.addActionListener(a -> {
            if (editor.editing != null) {
                editor.editing.cbsMode = cbsEditor.getItemAt(cbsEditor.getSelectedIndex());
                ConfigEntity.changed();
            }
        });
    }

    void edit(SafetyInfo editing) {
        runEditor.setEnabled(editing != null);
        jumpEditor.setEnabled(editing != null && editing.type == SafetyInfo.Type.PORTAL);
        cbsEditor.setEnabled(editing != null && editing.type == SafetyInfo.Type.CBS);

        setEdit(runEditor, editing == null ? null : editing.runMode);
        setEdit(jumpEditor, editing == null ? null : editing.jumpMode);
        setEdit(cbsEditor, editing == null ? null : editing.cbsMode);
    }

    private <T> void setEdit(JComboBox<T> combo, T obj) {
        if (obj == null) combo.setSelectedIndex(-1);
        else combo.setSelectedItem(obj);
    }

}
