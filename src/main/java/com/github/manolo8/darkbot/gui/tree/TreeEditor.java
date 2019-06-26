package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.tree.ConfigNode;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.components.JLabelField;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

public class TreeEditor extends DefaultTreeCellEditor {

    private boolean leaf;

    private Map<Class, OptionEditor> editorsByType = new HashMap<>();
    private Map<Class<? extends OptionEditor>, OptionEditor> editorsByClass = new HashMap<>();
    private OptionEditor defaultEditor = new JLabelField();

    private JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private JLabel label = new JLabel(){
        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.height = leaf ? AdvancedConfig.ROW_HEIGHT : AdvancedConfig.HEADER_HEIGHT;
            return d;
        }
    };
    private OptionEditor currentEditor = null;

    public TreeEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);

        this.label.setFont(renderer.getFont());
        this.panel.add(label);
        this.panel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
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
        return editorsByClass.computeIfAbsent(editorClass,
                c -> ReflectionUtils.createInstance(c, (Class<Object>) field.parent.getClass(), field.parent));
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
                                                boolean expanded, boolean leaf, int row) {
        this.leaf = leaf;

        ConfigNode node = ((ConfigNode) value);
        label.setText(node.name + (leaf ? ": " : ""));

        if (currentEditor != null) panel.remove(currentEditor.getComponent());
        if (leaf) {
            ConfigNode.Leaf option = (ConfigNode.Leaf) node;
            currentEditor = getEditor(option.field);
            currentEditor.edit(option.field);
            panel.add(currentEditor.getComponent());
        } else {
            currentEditor = null;
            if (expanded) tree.collapseRow(row);
            else tree.expandRow(row);
        }
        return panel;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        if (e == null || e.getSource() != tree || !(e instanceof MouseEvent)) return false;

        ((MouseEvent) e).consume();
        lastPath = tree.getClosestPathForLocation(((MouseEvent)e).getX(), ((MouseEvent)e).getY());
        return lastPath != null;
    }

}
