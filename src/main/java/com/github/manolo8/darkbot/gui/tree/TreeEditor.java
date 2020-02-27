package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.tree.ConfigNode;
import com.github.manolo8.darkbot.gui.tree.components.JBoolField;
import com.github.manolo8.darkbot.gui.tree.components.JCharField;
import com.github.manolo8.darkbot.gui.tree.components.JLabelField;
import com.github.manolo8.darkbot.gui.tree.components.JNumberField;
import com.github.manolo8.darkbot.gui.tree.components.JRangeField;
import com.github.manolo8.darkbot.gui.tree.components.JShipConfigField;
import com.github.manolo8.darkbot.gui.tree.components.JStringField;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

public class TreeEditor extends DefaultTreeCellEditor {

    // Standard editors for specific data types
    private Map<Class, OptionEditor> editorsByType = new HashMap<>();
    // Editors shared between diff TreeEditor instances
    private Map<Class<? extends OptionEditor>, OptionEditor> sharedEditors = new HashMap<>();
    // Editors for specific @Editor annotations in fields
    private Map<Class<? extends OptionEditor>, OptionEditor> editorsByClass = new HashMap<>();
    private OptionEditor defaultEditor = new JLabelField();

    private JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private JLabelField label = new JLabelField();
    private OptionEditor currentEditor = null;

    public TreeEditor(JTree tree, TreeRenderer renderer) {
        super(tree, renderer);

        this.label.setFont(renderer.getFont());
        this.panel.add(label);
        panel.setOpaque(false);
        defaultEditor.getComponent().setOpaque(false);

        addEditor(new JCharField(), Character.class);
        addEditor(new JBoolField(), boolean.class);
        addEditor(new JNumberField(), double.class, int.class);
        addEditor(new JStringField(), String.class);
        addEditor(new JShipConfigField(), Config.ShipConfig.class);
        addEditor(new JRangeField(), Config.PercentRange.class);
    }

    public JTree getTree() {
        return tree;
    }

    public TreePath getLastPath() {
        return lastPath;
    }

    /**
     * Shared editors are used by several tree editors, to keep consistency of the marked settings.
     * Used by NPC / BOX tables, so that filters stay consistent.
     */
    public TreeEditor sharingEditors(TreeEditor other) {
        this.sharedEditors = other.sharedEditors;
        return this;
    }

    private void addEditor(OptionEditor editor, Class... types) {
        for (Class type : types) this.editorsByType.put(type, editor);
        editor.getComponent().setOpaque(false);
    }

    private OptionEditor getEditor(ConfigField field) {
        Class<? extends OptionEditor> editorClass = field.getEditor();
        if (field.getEditor() == null) return editorsByType.getOrDefault(field.field.getType(), defaultEditor);
        Map<Class<? extends OptionEditor>, OptionEditor> editorMap = field.isSharedEditor() ? sharedEditors : editorsByClass;
        return editorMap.computeIfAbsent(editorClass,
                c -> ReflectionUtils.createInstance(c, (Class<Object>) field.parent.getClass(), field.parent));
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
                                                boolean expanded, boolean leaf, int row) {

        ConfigNode node = ((ConfigNode) value);
        label.setText(I18n.getOrDefault(node.key, node.name));
        label.setPreferredSize(new Dimension(getWidthFor(node, label.getFontMetrics(label.getFont())), 0));

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
        panel.setToolTipText(I18n.getOrDefault(node.key + ".desc", node.description));
        return panel;
    }

    private int getWidthFor(ConfigNode node, FontMetrics font) {
        if (I18n.getOrDefault(node.key, node.name).isEmpty()) return 0;
        return ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.DISPLAY.HIDE_EDITORS ?
                font.stringWidth(I18n.getOrDefault(node.key, node.name)) + 5 :
                font.stringWidth(node.getLongestSibling()) + 10;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        if (e == null || e.getSource() != tree || !(e instanceof MouseEvent)) return false;

        lastPath = tree.getClosestPathForLocation(((MouseEvent)e).getX(), ((MouseEvent)e).getY());
        return lastPath != null && tree.getModel().isLeaf(lastPath.getLastPathComponent());
    }

}
