package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.tree.ConfigNode;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;
import java.awt.*;

public class TreeCell extends JPanel {
    private final JComponent EMPTY_EDITOR = new JLabel();

    private final EditorManager editors;

    private int nameWidth;
    private int minHeight;

    private final JLabel name = new JLabel();
    private JComponent editor = EMPTY_EDITOR;

    public TreeCell(EditorManager editors) {
        setLayout(new TreeCellLayout());

        this.editors = editors;
        setOpaque(false);

        add(name);
        add(editor);
    }

    public void setEditing(ConfigNode node) {
        minHeight = getMinHeight(node);
        nameWidth = editors.getWidthFor(node, name.getFontMetrics(name.getFont()));

        name.setText(I18n.getOrDefault(node.key, node.name));

        if (node instanceof ConfigNode.Leaf) {
            ConfigNode.Leaf leaf = (ConfigNode.Leaf) node;
            setEditor(editors.getEditor(leaf.field), leaf.field);
        } else {
            setEditor(null, null);
        }

        setToolTipText(I18n.getOrDefault(node.key + ".desc", node.description));
    }

    private void setEditor(OptionEditor newEditor, ConfigField field) {
        if (editor != null) remove(editor);
        if (newEditor != null) {
            newEditor.edit(field);
            add(editor = newEditor.getComponent());
        } else {
            add(editor = EMPTY_EDITOR);
        }
    }

    /**
     * Minimum height based on the depth. Lower depth nodes should be taller
     * @param node The node to get height for
     * @return the minimum height the component should have
     */
    private int getMinHeight(ConfigNode node) {
        return node instanceof ConfigNode.Leaf || node.getDepth() > 1 ?  AdvancedConfig.ROW_HEIGHT : AdvancedConfig.HEADER_HEIGHT;
    }

    private class TreeCellLayout implements LayoutManager {
        @Override
        public void addLayoutComponent(String name, Component comp) {}
        @Override
        public void removeLayoutComponent(Component comp) {}

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Dimension dim = parent.getComponent(1).getPreferredSize();
            dim.width += nameWidth;
            dim.height = Math.max(dim.height, minHeight);
            return dim;
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent); // Serves no real purpose
        }

        @Override
        public void layoutContainer(Container parent) {
            Dimension editorSize = parent.getComponent(1).getPreferredSize();

            int height = Math.max(minHeight, editorSize.height);
            parent.getComponent(0).setBounds(0, 0, nameWidth, height);
            parent.getComponent(1).setBounds(nameWidth, (height - editorSize.height) / 2, editorSize.width, editorSize.height);
        }
    }

}
