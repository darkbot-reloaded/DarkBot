package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import eu.darkbot.api.config.ConfigSetting;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Stream;

public class TreeCell extends JPanel {
    private final JComponent EMPTY_EDITOR = new JLabel();

    private final EditorProvider editors;

    private int nameWidth;
    private int minHeight;

    private final JLabel name = new JLabel();
    private JComponent component = EMPTY_EDITOR;
    private ConfigSetting<?> setting;


    private OptionEditor legacyEditor;
    private eu.darkbot.api.config.util.OptionEditor<?> editor;

    public TreeCell(EditorProvider editors) {
        setLayout(new TreeCellLayout());

        this.editors = editors;
        setOpaque(false);

        add(name);
        add(component);
    }

    public <T> void setEditing(ConfigSetting<T> setting) {
        this.setting = setting;
        minHeight = getMinHeight(setting);
        nameWidth = getWidthFor(setting, name.getFontMetrics(name.getFont()));

        name.setText(setting.getName());
        setToolTipText(setting.getDescription());

        if (!(setting instanceof ConfigSetting.Parent)) {
            eu.darkbot.api.config.util.OptionEditor<T> editor = editors.getEditor(setting);

            try {
                if (editor != null) {
                    this.editor = editor;
                    this.legacyEditor = null;

                    if (component != null) {
                        remove(component);
                        component = null;
                    }

                    add(component = editor.getEditorComponent(setting));
                } else {
                    ConfigField cf = new ConfigField(setting);
                    this.editor = null;
                    this.legacyEditor = editors.getLegacyEditor(cf);

                    if (component != null) {
                        remove(component);
                        component = null;
                    }

                    legacyEditor.edit(cf);
                    add(component = legacyEditor.getComponent());
                }

                return;
            } catch (Throwable e) {
                System.out.println("Error setting up editor, editor won't show: ");
                e.printStackTrace();
            }
        }
        this.editor = null;
        this.legacyEditor = null;

        if (component != null) remove(component);
        add(component = EMPTY_EDITOR);
    }

    public Object getValue() {
        return editor != null ? editor.getEditorValue() : setting.getValue();
    }

    public boolean stopCellEditing() {
        return editor == null || editor.stopCellEditing();
    }

    public void cancelCellEditing() {
        if (editor != null) editor.cancelCellEditing();
    }

    /**
     * Minimum height based on the depth. Lower depth nodes should be taller
     * @param node The node to get height for
     * @return the minimum height the component should have
     */
    private int getMinHeight(ConfigSetting<?> node) {
        return !(node instanceof ConfigSetting.Parent) ||
                (node.getParent() != null && node.getParent().getParent() != null) ?
                AdvancedConfig.ROW_HEIGHT : AdvancedConfig.HEADER_HEIGHT;
    }

    /**
     * Minimum width based on the name of all siblings
     * @param node config setting node to check siblings of
     * @param font the font in which to measure size
     * @return the width reserved for the name
     */
    private int getWidthFor(ConfigSetting<?> node, FontMetrics font) {
        if (node.getName().isEmpty()) return 0;
        if (node instanceof ConfigSetting.Parent) return font.stringWidth(node.getName()) + 5;

        ConfigSetting.Parent<?> parent = node.getParent();
        return (parent == null ? Stream.of(node) : parent.getChildren().values().stream())
                .filter(cs -> !(cs instanceof ConfigSetting.Parent))
                .map(ConfigSetting::getName)
                .mapToInt(font::stringWidth)
                .max()
                .orElseGet(() -> font.stringWidth(node.getName())) + 10;
    }

    private class TreeCellLayout implements LayoutManager {
        @Override
        public void addLayoutComponent(String name, Component comp) {}
        @Override
        public void removeLayoutComponent(Component comp) {}

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Dimension dim = parent.getComponent(1).getPreferredSize();
            Dimension res = null;

            if (editor != null) res = editor.getReservedSize();
            else if (legacyEditor != null) res = legacyEditor.getReservedSize();

            if (res != null)
                dim.setSize(Math.max(dim.width, res.width), Math.max(dim.height, res.height));


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
