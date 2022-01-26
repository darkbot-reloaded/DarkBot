package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.gui.utils.tree.ConfigSettingTreeModel;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.extensions.plugins.PluginListener;
import com.github.manolo8.darkbot.gui.tree.ConfigTree;
import com.github.manolo8.darkbot.gui.tree.EditorProvider;
import com.github.manolo8.darkbot.gui.utils.SearchField;
import com.github.manolo8.darkbot.gui.utils.tree.CompoundConfigSetting;
import com.github.manolo8.darkbot.gui.utils.tree.PluginListConfigSetting;
import com.github.manolo8.darkbot.gui.utils.tree.SimpleConfigSettingRenderer;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.util.Iterator;

public class AdvancedConfig extends JPanel implements PluginListener {

    public static final int EDITOR_HEIGHT = 17;
    public static final int ROW_HEIGHT = 19;
    public static final int HEADER_HEIGHT = 26;

    private final PluginAPI api;

    private ConfigSetting.Parent<?> baseConfig, extendedConfig, lastSelection;

    private final TreeTabsModel tabsModel = new TreeTabsModel();
    private final ConfigSettingTreeModel treeModel = new ConfigSettingTreeModel();

    private JTree tabsTree;
    private ConfigTree configTree;

    private boolean packed = false; // If this is a packed config in a floating window

    public AdvancedConfig(PluginAPI api) {
        super(new MigLayout("ins 0, gap 0, fill, wrap 1", "[]", "[][grow]"));
        this.api = api;
    }

    @Deprecated
    public AdvancedConfig(Object obj) {
        this.api = null;
    }

    public AdvancedConfig(PluginAPI api, ConfigSetting.Parent<?> config) {
        super(new BorderLayout());

        this.api = api;
        this.packed = true;
        setEditingConfig(config);
        rebuildUI();
    }

    public void setEditingConfig(ConfigSetting.Parent<?> config) {
        this.baseConfig = config;
        this.extendedConfig = packed ? config : new CompoundConfigSetting<>(this.baseConfig,
                new PluginListConfigSetting(baseConfig, api.requireInstance(FeatureRegistry.class)));
        this.lastSelection = config;
        if (!packed) {
            Iterator<ConfigSetting<?>> children = config.getChildren().values().iterator();
            if (children.hasNext()) {
                ConfigSetting<?> child = children.next();
                if (child instanceof ConfigSetting.Parent)
                    lastSelection = (ConfigSetting.Parent<?>) child;
            }
        }
    }

    void rebuildUI() {
        if (this.baseConfig == null ||
                this.extendedConfig == null ||
                (packed && this.lastSelection == null)) return;

        removeAll();
        treeModel.clearListeners();
        tabsModel.clearListeners();
        setCorrectRoot();
        if (!packed) {
            tabsTree = new JTree(tabsModel);
            tabsTree.setCellRenderer(new SimpleConfigSettingRenderer());
            tabsTree.setRootVisible(false);
            tabsTree.setShowsRootHandles(true);
            tabsTree.getSelectionModel().addTreeSelectionListener(e -> {
                if (configTree != null) configTree.stopEditing(); // Save any midway edition

                if (treeModel.isFiltered()) scrollToPath(e.getPath());
                else treeModel.setRoot(lastSelection = (ConfigSetting.Parent<?>) e.getPath().getLastPathComponent());
            });
            tabsTree.setRowHeight(24);

            add(new SearchField(this::setSearch), "grow");

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    wrapInScrollPane(tabsTree),
                    setupUI());
            add(splitPane, "grow");
        } else {
            add(setupUI());
        }
        this.revalidate();
        this.repaint();
    }

    private void scrollToPath(TreePath path) {
        if (configTree == null) return;

        configTree.makeVisible(path);

        Rectangle bounds = configTree.getPathBounds(path);
        if (bounds != null) {
            bounds.height = configTree.getVisibleRect().height;
            configTree.scrollRectToVisible(bounds);
        }
    }

    @Override
    public void beforeLoad() {
        removeAll();
        this.extendedConfig = null;
        this.lastSelection = null;
    }

    @Override
    public void afterLoadCompleteUI() {
        setEditingConfig(this.baseConfig);
        rebuildUI();
    }

    public void setCustomConfig(@Nullable ConfigSetting.Parent<?> config) {
        this.extendedConfig = new CompoundConfigSetting<>(this.baseConfig,
                config,
                new PluginListConfigSetting(baseConfig, api.requireInstance(FeatureRegistry.class)));
        setCorrectRoot();
    }

    public void updateConfigTreeListeners() {
        treeModel.updateListeners();
        tabsModel.updateListeners();
    }

    private void setSearch(String search) {
        boolean wasFiltered = treeModel.isFiltered();
        treeModel.setSearch(search);
        tabsModel.setSearch(search);

        if (wasFiltered != treeModel.isFiltered()) {
            setCorrectRoot();
        } else {
            treeModel.updateListeners();
            tabsModel.updateListeners();
        }
    }

    private void setCorrectRoot() {
        treeModel.setRoot(packed ? baseConfig :
                treeModel.isFiltered() ?
                        extendedConfig != null ? extendedConfig : baseConfig :
                        lastSelection != null ? lastSelection : baseConfig);
        tabsModel.setRoot(extendedConfig != null ? extendedConfig : baseConfig);
    }

    private JComponent setupUI() {
        EditorProvider renderer = api.requireInstance(EditorProvider.class);
        EditorProvider editor = new EditorProvider(renderer);

        configTree = new ConfigTree(treeModel, renderer, editor);

        JScrollPane scrollPane = wrapInScrollPane(configTree);

        if (packed) {
            Dimension treeSize = configTree.getPreferredSize();
            scrollPane.setPreferredSize(new Dimension(treeSize.width + 15, Math.min(400, treeSize.height)));
        }
        return new JLayer<>(scrollPane, new WheelScrollLayerUI());
    }

    private static JScrollPane wrapInScrollPane(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(25);
        return scrollPane;
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
            if (c instanceof JLayer) ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        }

        @Override
        public void uninstallUI(JComponent c) {
            if (c instanceof JLayer) ((JLayer<?>) c).setLayerEventMask(0);
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

    private static class TreeTabsModel extends ConfigSettingTreeModel {

        @Override
        public boolean isLeaf(Object node) {
            if (super.isLeaf(node)) return true;
            return ((ConfigSetting.Parent<?>) node).getChildren().values().stream().anyMatch(super::isLeaf);
        }
    }

}
