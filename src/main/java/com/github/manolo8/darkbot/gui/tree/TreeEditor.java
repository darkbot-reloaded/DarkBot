package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.tree.ConfigNode;
import com.github.manolo8.darkbot.gui.tree.components.JLabelField;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

public class TreeEditor extends DefaultTreeCellEditor {

    private EditorManager editors;

    private JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private JLabelField label = new JLabelField();
    private OptionEditor currentEditor = null;

    public TreeEditor(JTree tree, TreeRenderer renderer, EditorManager editors) {
        super(tree, renderer);
        this.editors = editors;

        this.label.setFont(renderer.getFont());
        this.panel.add(label);
        panel.setOpaque(false);
    }

    public JTree getTree() {
        return tree;
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
                                                boolean expanded, boolean leaf, int row) {

        ConfigNode node = ((ConfigNode) value);
        label.setText(I18n.getOrDefault(node.key, node.name));
        label.setPreferredSize(new Dimension(editors.getWidthFor(node, label.getFontMetrics(label.getFont())), 0));

        if (currentEditor != null) panel.remove(currentEditor.getComponent());
        if (leaf) {
            ConfigNode.Leaf option = (ConfigNode.Leaf) node;
            currentEditor = editors.getEditor(option.field);
            currentEditor.edit(option.field);
            panel.add(currentEditor.getComponent());
        } else {
            currentEditor = null;
            if (expanded) tree.collapseRow(row);
            else tree.expandRow(row);
        }
        panel.setToolTipText(I18n.getOrDefault(node.key + ".desc", node.description));
        return panel;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        if (e == null || e.getSource() != tree || !(e instanceof MouseEvent)) return false;

        lastPath = tree.getClosestPathForLocation(((MouseEvent)e).getX(), ((MouseEvent)e).getY());
        return lastPath != null && tree.getModel().isLeaf(lastPath.getLastPathComponent());
    }

}
