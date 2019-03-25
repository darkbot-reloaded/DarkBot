package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.tree.ConfigNode;
import com.github.manolo8.darkbot.gui.tree.components.JLabelField;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

public class TreeEditor extends DefaultTreeCellEditor {

    private Map<Class, OptionEditor> editorsByType = new HashMap<>();
    private Map<Class<? extends OptionEditor>, OptionEditor> editorsByClass = new HashMap<>();
    private OptionEditor defaultEditor = new JLabelField();

    private JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    private JLabel label = new JLabel();
    private OptionEditor currentEditor = null;

    public TreeEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);

        this.label.setFont(renderer.getFont());
        this.panel.add(label);
        this.panel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        panel.setOpaque(false);
        defaultEditor.getComponent().setOpaque(false);
    }

    public void addEditor(OptionEditor editor, Class... types) {
        for (Class type : types) this.editorsByType.put(type, editor);
        editor.getComponent().setOpaque(false);
    }

    private OptionEditor getEditor(ConfigField field) {
        Class<? extends OptionEditor> editorClass = field.getEditor();
        if (field.getEditor() == null) return editorsByType.getOrDefault(field.field.getType(), defaultEditor);
        return editorsByClass.computeIfAbsent(editorClass, c -> ReflectionUtils.createInstance(c, (Class<Object>) field.parent.getClass(), field.parent));
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
                                                boolean expanded, boolean leaf, int row) {
        if (!leaf) return super.getTreeCellEditorComponent(tree, ((ConfigNode) value).name, isSelected, expanded, false, row);

        ConfigNode.Leaf option = ((ConfigNode.Leaf) value);
        label.setText(option.name + ": ");

        if (currentEditor != null) panel.remove(currentEditor.getComponent());
        currentEditor = getEditor(option.field);
        currentEditor.edit(option.field);
        JComponent comp = currentEditor.getComponent();
        comp.setPreferredSize(new Dimension(comp.getPreferredSize().width, renderer.getPreferredSize().height));
        panel.add(currentEditor.getComponent());
        return panel;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        if (e == null || e.getSource() != tree || !(e instanceof MouseEvent)) return false;

        TreePath path = tree.getPathForLocation(((MouseEvent)e).getX(), ((MouseEvent)e).getY());
        return lastPath != null && lastPath.equals(lastPath = path) && lastPath.getLastPathComponent() instanceof ConfigNode.Leaf;
    }

}
