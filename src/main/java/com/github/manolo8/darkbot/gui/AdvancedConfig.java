package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.config.tree.ConfigTree;
import com.github.manolo8.darkbot.extensions.plugins.PluginListener;
import com.github.manolo8.darkbot.gui.utils.SearchField;
import com.github.manolo8.darkbot.gui.tree.EditorManager;
import com.github.manolo8.darkbot.gui.tree.TreeEditor;
import com.github.manolo8.darkbot.gui.tree.TreeRenderer;
import com.github.manolo8.darkbot.gui.utils.SimpleTreeListener;
import net.miginfocom.swing.MigLayout;

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
    private boolean packed = false; // If this is a packed config in a floating window

    public AdvancedConfig() {
        setLayout(new MigLayout("ins 0, gap 0, fill, wrap 1", "[]", "[][grow]"));
    }

    public AdvancedConfig(Object config) {
        setLayout(new BorderLayout());
        packed = true;
        setEditingConfig(config);
    }

    void setEditingConfig(Object config) {
        if (config == null) return;
        removeAll();
        this.config = config;
        this.treeModel = new ConfigTree(config);
        if (!packed) {
            add(new SearchField(treeModel::setFilter), "grow");
            add(setupUI(), "grow");
        } else {
            add(setupUI());
        }
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
        JTree configTree = new JTree(treeModel);
        configTree.setEditable(true);
        configTree.setRootVisible(false);
        configTree.setShowsRootHandles(true);
        configTree.setToggleClickCount(1);
        configTree.setRowHeight(0);
        configTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        ToolTipManager.sharedInstance().registerComponent(configTree);

        EditorManager editors = new EditorManager();

        configTree.setCellRenderer(new TreeRenderer(editors));
        configTree.setCellEditor(new TreeEditor(configTree, new EditorManager(editors)));

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
