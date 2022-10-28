package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.utils.FocusEventUtil;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.tree.PluginListConfigSetting;
import eu.darkbot.api.config.ConfigSetting;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TreeCell extends JPanel {
    private final JComponent EMPTY_EDITOR = new JLabel(),
            ERROR_EDITOR = UIUtils.setRed(new JLabel("Error creating editor (hover for details)"), true);

    private final EditorProvider editors;
    private final boolean isEditor; // Editor = true, Renderer = false

    private int nameWidth;
    private int minHeight;

    private final JLabel name = new JLabel();
    private JComponent component = EMPTY_EDITOR;
    private ConfigSetting<?> setting;

    private OptionEditor legacyEditor;
    private eu.darkbot.api.config.util.OptionEditor<?> editor;

    private final List<Component> focusableComponents = new ArrayList<>();
    private final FocusListener focusListener = new EditorFocusListener();

    public TreeCell(EditorProvider editors, boolean editor) {
        setLayout(new TreeCellLayout());

        this.editors = editors;
        this.isEditor = editor;
        setOpaque(false);

        add(name);
        add(component);
        if (isEditor) {
            add(new EditorButton(UIUtils.getIcon("tick"),
                    "Save edition\nDefault if you click outside the editor", JTree::stopEditing));
            add(new EditorButton(UIUtils.getIcon("cross"),
                    "Cancel edition\nCan also be triggered by ESC", JTree::cancelEditing));
        }
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

                    replaceComponent(editor.getEditorComponent(setting, isEditor));
                } else {
                    ConfigField cf = new ConfigField(setting);
                    this.editor = null;
                    this.legacyEditor = editors.getLegacyEditor(cf);

                    legacyEditor.edit(cf);
                    replaceComponent(legacyEditor.getComponent());
                }
            } catch (Throwable e) {
                ERROR_EDITOR.setToolTipText(IssueHandler.createDescription(e));
                replaceComponent(ERROR_EDITOR);
            }
            return;
        }
        this.editor = null;
        this.legacyEditor = null;

        replaceComponent(EMPTY_EDITOR);
    }

    private void replaceComponent(JComponent component) {
        if (this.component != null) {
            if (isEditor) {
                focusableComponents.forEach(c -> c.removeFocusListener(focusListener));
                focusableComponents.clear();
            }
            remove(this.component);
        }
        add(this.component = component, 1);
        if (isEditor) registerFocusListener(this.component);
    }

    private void registerFocusListener(Component component) {
        if (component.isFocusable()) {
            focusableComponents.add(component);
            component.addFocusListener(focusListener);
        }
        if (component instanceof Container) {
            for (Component c : ((Container) component).getComponents()) {
                registerFocusListener(c);
            }
        }
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
        return node instanceof ConfigSetting.Parent &&
                (isRoot(node) || isRoot(node.getParent())) ?
                AdvancedConfig.HEADER_HEIGHT : AdvancedConfig.ROW_HEIGHT;
    }

    private boolean isRoot(@Nullable ConfigSetting<?> node) {
        return node != null && (node.getParent() == null || node.getParent() instanceof PluginListConfigSetting);
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


            dim.width += nameWidth + (showButtons(parent.getComponent(1)) ? 3 + 18 + 18 : 0);
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
            if (showButtons(parent.getComponent(1)) && isEditor) {
                parent.getComponent(2).setBounds(nameWidth + editorSize.width + 3     , 0, 18, height);
                parent.getComponent(3).setBounds(nameWidth + editorSize.width + 3 + 18, 0, 18, height);
            }
        }

        private boolean showButtons(Component comp) {
            return nameWidth > 0 && !(comp instanceof JLabel)
                    && !Boolean.TRUE.equals(setting.getMetadata("readonly"));
        }
    }

    private static class EditorFocusListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {}

        @Override
        public void focusLost(FocusEvent e) {
            if (shouldStopEditing(e)) {
                JTree tree = (JTree) SwingUtilities.getAncestorOfClass(JTree.class, e.getComponent());
                if (tree != null) tree.stopEditing();
            }
        }

        private boolean shouldStopEditing(FocusEvent e) {
            Component component = e.getComponent();

            // We wouldn't be able to find the tree even if we wanted to stop.
            if (component == null) return false;

            // Another window has been activated, always stop editing
            if (FocusEventUtil.isWindowActivation(e)) return true;

            // Temporary focus loss, this is triggered by dropdown menus and similar things
            if (e.isTemporary()) return false;

            // opposite is null sometimes when switching between 2 editors of same type, not enough info to decide
            Component opposite = e.getOppositeComponent();
            if (opposite == null) return false;

            // Both components are in the same editor (eg: Switching inside range editor min & max), do nothing
            if (component.getFocusCycleRootAncestor() == opposite.getFocusCycleRootAncestor()) return false;

            // Opposite component isn't even in a tree (it's something outside the tree), stop the edition
            JTree oppositeTree = getTree(opposite);
            if (oppositeTree == null) return true;

            // If they belong on the same tree, do nothing, tree handles it on its own
            JTree componentTree = getTree(component);
            return componentTree != oppositeTree;
        }

        private JTree getTree(Component component) {
            if (component instanceof JTree) return (JTree) component;
            return (JTree) SwingUtilities.getAncestorOfClass(JTree.class, component);
        }

    }

    private static class EditorButton extends JButton {

        public EditorButton(ImageIcon icon, String tooltip, Consumer<JTree> action) {
            super(icon);
            setFocusable(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setToolTipText(tooltip);

            addActionListener(e ->
                    action.accept((JTree) SwingUtilities.getAncestorOfClass(JTree.class, EditorButton.this)));
        }
    }

}
