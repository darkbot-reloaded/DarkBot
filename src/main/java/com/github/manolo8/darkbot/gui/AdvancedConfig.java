package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.config.tree.ConfigTree;
import com.github.manolo8.darkbot.extensions.plugins.PluginListener;
import com.github.manolo8.darkbot.gui.tree.EditorManager;
import com.github.manolo8.darkbot.gui.tree.TreeEditor;
import com.github.manolo8.darkbot.gui.tree.TreeRenderer;
import com.github.manolo8.darkbot.gui.utils.SimpleTreeListener;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseWheelEvent;

public class AdvancedConfig extends JPanel implements PluginListener {

    public static final int EDITOR_HEIGHT = 17;
    public static final int ROW_HEIGHT = 18;
    public static final int HEADER_HEIGHT = 26;

    private Object config;
    private ConfigTree treeModel;
    private boolean packed = false;

    public AdvancedConfig() {
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());
    }

    public AdvancedConfig(Object config) {
        this();
        packed = true;
        setEditingConfig(config);
    }

    void setEditingConfig(Object config) {
        if (config == null) return;
        removeAll();
        this.config = config;
        add(setupUI());
        this.revalidate();
        this.repaint();
    }

    @Override
    public void beforeLoad() {
        removeAll();
    }

    @Override
    public void afterLoadComplete() {
        setEditingConfig(config);
    }

    public void setCustomConfig(String name, Object config) {
        treeModel.setCustom(name, config);
    }

    private JComponent setupUI() {
        JTree configTree = new JTree(this.treeModel = new ConfigTree(config));
        configTree.setEditable(true);
        configTree.setRootVisible(false);
        configTree.setShowsRootHandles(true);
        configTree.setToggleClickCount(1);
        configTree.setRowHeight(0);
        configTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        ToolTipManager.sharedInstance().registerComponent(configTree);

        EditorManager editors = new EditorManager();

        TreeRenderer renderer = new TreeRenderer(editors);
        configTree.setCellRenderer(renderer);
        configTree.setCellEditor(new TreeEditor(configTree, renderer, new EditorManager(editors)));

        treeModel.addTreeModelListener((SimpleTreeListener) e -> {
            unfoldTopLevelTree(configTree);
            SwingUtilities.invokeLater(() -> {
                configTree.validate();
                configTree.repaint();
            });
        });
        unfoldTopLevelTree(configTree);

        JScrollPane scrollPane = new JScrollPane(configTree);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(25);

        if (packed) {
            configTree.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 3));
            Dimension treeSize = configTree.getPreferredSize();
            scrollPane.setPreferredSize(new Dimension(treeSize.width + 15, Math.min(400, treeSize.height)));
        }
        return new JLayer<>(scrollPane, new WheelScrollLayerUI());
    }

    private void unfoldTopLevelTree(JTree configTree) {
        for (int i = configTree.getRowCount() - 1; i >= 0; i--) configTree.expandRow(i);
    }

    public static Dimension forcePreferredHeight(Dimension preferred) {
        preferred.height = EDITOR_HEIGHT;
        return preferred;
    }

    // http://java-swing-tips.blogspot.jp/2014/09/forward-mouse-wheel-scroll-event-in.html
    static class WheelScrollLayerUI extends LayerUI<JScrollPane> {
        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            if (c instanceof JLayer) ((JLayer) c).setLayerEventMask(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        }

        @Override
        public void uninstallUI(JComponent c) {
            if (c instanceof JLayer) ((JLayer) c).setLayerEventMask(0);
            super.uninstallUI(c);
        }

        @Override
        protected void processMouseWheelEvent(MouseWheelEvent e, JLayer<? extends JScrollPane> l) {
            Component child = e.getComponent();
            JScrollPane parent = l.getView();
            if (!(child instanceof JScrollPane) || child.equals(parent)) return;
            BoundedRangeModel m = ((JScrollPane) child).getVerticalScrollBar().getModel();
            int dir = e.getWheelRotation(), extent = m.getExtent(),
                    minimum = m.getMinimum(), maximum = m.getMaximum(), value = m.getValue();
            if (value + extent >= maximum && dir > 0 || value <= minimum && dir < 0)
                parent.dispatchEvent(SwingUtilities.convertMouseEvent(child, e, parent));
        }
    }

}
