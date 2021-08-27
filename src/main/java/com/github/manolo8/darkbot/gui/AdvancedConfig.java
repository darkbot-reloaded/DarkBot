package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.config.tree.ConfigSettingTree;
import com.github.manolo8.darkbot.config.tree.TreeFilter;
import com.github.manolo8.darkbot.extensions.plugins.PluginListener;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.tree.EditorManager;
import com.github.manolo8.darkbot.gui.tree.TreeEditor;
import com.github.manolo8.darkbot.gui.tree.TreeRenderer;
import com.github.manolo8.darkbot.gui.utils.SearchField;
import com.github.manolo8.darkbot.gui.utils.SimpleTreeListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import eu.darkbot.api.config.ConfigSetting;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.LayerUI;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdvancedConfig extends JPanel implements PluginListener {

    public static final int EDITOR_HEIGHT = 17;
    public static final int ROW_HEIGHT = 18;
    public static final int HEADER_HEIGHT = 26;

    private ConfigSetting.Parent<?> config;
    private final ConfigSettingTree treeModel = new ConfigSettingTree();
    private JPanel tabs;
    private final Map<String, TabButton> buttons = new LinkedHashMap<>();
    private boolean packed = false; // If this is a packed config in a floating window

    public AdvancedConfig() {
        setLayout(new MigLayout("ins 0, gap 0, fill, wrap 2", "[][grow]", "[][grow]"));
    }

    public AdvancedConfig(ConfigSetting.Parent<?> config) {
        setLayout(new BorderLayout());
        this.packed = true;
        setEditingConfig(config);
    }

    void setEditingConfig(ConfigSetting.Parent<?> config) {
        if (config == null) return;
        removeAll();
        this.config = config;
        treeModel.setRoot(config);
        if (!packed) {
            treeModel.setRoot((ConfigSetting.Parent<?>) config.getChildren().values().iterator().next());

            tabs = new JPanel(new MigLayout("ins 0, gap 0, wrap 1", "[]"));

            add(new SearchField(this::setSearch), "span 2, grow");
            add(tabs, "grow");
            add(setupUI(), "grow");
            updateTabs();
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
    public void afterLoadCompleteUI() {
        setEditingConfig(config);
    }

    public void setCustomConfig(String name, Object config) {
        //treeModel.setCustom(name, config);
        updateTabs();
    }

    private void setSearch(String search) {
        TreeFilter filter = treeModel.getFilter();
        boolean prevUnfiltered = filter.isUnfiltered();
        treeModel.getFilter().setSearch(search);
        treeModel.updateListeners();
        if (prevUnfiltered != filter.isUnfiltered()) updateTabs();
    }

    private void updateTabs() {
        if (tabs == null) return;
        tabs.removeAll();
        if (treeModel.getFilter().isUnfiltered()) {
            for (Map.Entry<String, ConfigSetting<?>> entry : config.getChildren().entrySet()) {
                String key = entry.getKey();
                TabButton tb = buttons.computeIfAbsent(key,
                        k -> new TabButton((ConfigSetting.Parent<?>) entry.getValue()));
                tb.update();
                tabs.add(tb, "grow");
            }
        }

        tabs.revalidate();
        tabs.repaint();
    }

    private class TabButton extends MainButton {
        private final Border HIGHLIGHT = new MatteBorder(0, 0, 0, 3, UIUtils.TAB_HIGLIGHT);

        private final ConfigSetting.Parent<?> node;

        public TabButton(ConfigSetting.Parent<?> node) {
            super(node.getName());
            this.node = node;
            update();
        }

        public void update() {
            setBorder(treeModel.getRoot() == node ? HIGHLIGHT : null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            treeModel.setRoot(node);
            buttons.values().forEach(TabButton::update);
            treeModel.updateListeners();
        }
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

        EditorManager rendererEdManager = new EditorManager();
        EditorManager editorEdManager = new EditorManager(rendererEdManager);

        configTree.setCellRenderer(new TreeRenderer(rendererEdManager));
        configTree.setCellEditor(new TreeEditor(configTree, editorEdManager));

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
        for (int row = 0; row < configTree.getRowCount(); row++) {
            if (configTree.isExpanded(row)) continue;

            TreePath path = configTree.getPathForRow(row);
            if (treeModel.isLeaf(path.getLastPathComponent())) continue; // Ignore leaf nodes

            path = path.getParentPath();

            if (path == null || path.getPathCount() == 1) configTree.expandRow(row); // Unfold root or top-level nodes
            else {
                // Unfold children with no siblings
                int children = treeModel.getChildCount(path.getLastPathComponent());
                boolean hasLeaf = false;
                for (int child = 0; child < children; child++) {
                    if (!treeModel.isLeaf(treeModel.getChild(path.getLastPathComponent(), child))) continue;
                    hasLeaf = true;
                    break;
                }
                if (!hasLeaf) configTree.expandRow(row);
            }
        }
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
