package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.tree.ConfigBuilder;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.extensions.plugins.PluginListener;
import com.github.manolo8.darkbot.gui.tree.ConfigTree;
import com.github.manolo8.darkbot.gui.tree.EditorProvider;
import com.github.manolo8.darkbot.gui.tree.utils.DropdownRenderer;
import com.github.manolo8.darkbot.gui.utils.SearchField;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.WidthEnforcedScrollPane;
import com.github.manolo8.darkbot.gui.utils.tree.CompoundConfigSetting;
import com.github.manolo8.darkbot.gui.utils.tree.ConfigSettingTreeModel;
import com.github.manolo8.darkbot.gui.utils.tree.PluginListConfigSetting;
import com.github.manolo8.darkbot.gui.utils.tree.SimpleConfigSettingRenderer;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.annotations.Visibility;
import eu.darkbot.api.managers.ConfigAPI;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseWheelEvent;
import java.util.Iterator;

public class AdvancedConfig extends JPanel implements PluginListener {

    public static final int EDITOR_HEIGHT = 17;
    public static final int ROW_HEIGHT = 19;
    public static final int HEADER_HEIGHT = 26;

    private final PluginAPI api;
    private final ConfigSetting<Config.BotSettings.BotGui> guiConfig;

    private ConfigSetting.Parent<?> baseConfig, lastSelection;
    private @Nullable CompoundConfigSetting<?> extendedConfig;
    private PluginListConfigSetting pluginNode;

    private final TreeTabsModel tabsModel = new TreeTabsModel();
    private final ConfigSettingTreeModel treeModel = new ConfigSettingTreeModel();

    private JTree tabsTree;
    private ConfigTree configTree;

    private boolean packed = false; // If this is a packed config in a floating window

    public AdvancedConfig(PluginAPI api) {
        super(new MigLayout("ins 0, gap 0, fill", "[grow][]", "[][grow]"));
        this.api = api;
        this.guiConfig = api.requireAPI(ConfigAPI.class).requireConfig("bot_settings.bot_gui");
    }

    @Deprecated
    public AdvancedConfig(Object obj) {
        this(Main.INSTANCE.pluginAPI, createConfig(obj));
    }

    private static <T> ConfigSetting.Parent<T> createConfig(T obj) {
        ConfigBuilder cb = Main.INSTANCE.pluginAPI.requireInstance(ConfigBuilder.class);
        @SuppressWarnings("unchecked")
        Class<T> clz = (Class<T>) obj.getClass();
        ConfigSetting.Parent<T> p = cb.of(clz, "Root", null);
        p.setValue(obj);
        return p;
    }

    public AdvancedConfig(PluginAPI api, ConfigSetting.Parent<?> config) {
        super(new BorderLayout());

        this.api = api;
        this.guiConfig = api.requireAPI(ConfigAPI.class).requireConfig("bot_settings.bot_gui");
        this.packed = true;
        setEditingConfig(config);
        rebuildUI();
    }

    public void setEditingConfig(ConfigSetting.Parent<?> config) {
        this.baseConfig = config;
        this.extendedConfig = null;
        this.lastSelection = config;
        if (!packed) {
            this.pluginNode = new PluginListConfigSetting(baseConfig, api.requireInstance(FeatureRegistry.class));
            this.extendedConfig = new CompoundConfigSetting<>(baseConfig, pluginNode);

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
                (!packed && this.extendedConfig == null) ||
                (packed && this.lastSelection == null)) return;

        removeAll();
        treeModel.clearListeners();
        tabsModel.clearListeners();
        setCorrectRoot();
        if (!packed) {
            tabsTree = new JTree(tabsModel);
            BasicTreeUI treeUi = (BasicTreeUI) tabsTree.getUI();
            treeUi.setRightChildIndent(7);

            tabsTree.setCellRenderer(new SimpleConfigSettingRenderer());
            tabsTree.setRootVisible(false);
            tabsTree.setShowsRootHandles(true);
            tabsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            tabsTree.getSelectionModel().addTreeSelectionListener(e -> {
                if (configTree != null) configTree.stopEditing(); // Save any midway edition

                if (treeModel.isSearching()) scrollToPath(e.getPath());
                else if (lastSelection != e.getPath().getLastPathComponent())
                    treeModel.setRoot(lastSelection = (ConfigSetting.Parent<?>) e.getPath().getLastPathComponent());
            });
            tabsTree.setRowHeight(24);
            tabsTree.setBackground(UIUtils.BACKGROUND);
            tabsTree.setSelectionRow(0);

            add(new SearchField(this::setSearch), "grow");
            setSearch("");

            add(createVisibilityDropdown(guiConfig.getValue().CONFIG_LEVEL), "grow, wrap");
            setVisibility(guiConfig.getValue().CONFIG_LEVEL);

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    wrapInScrollPane(tabsTree, true),
                    setupUI());
            splitPane.setDividerLocation(0);
            splitPane.setDividerSize(3);

            ComponentListener cl = new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    int min = splitPane.getMinimumDividerLocation();
                    if (min > splitPane.getDividerLocation()) splitPane.setDividerLocation(min);
                }
            };

            tabsTree.addComponentListener(cl);
            splitPane.addComponentListener(cl);

            add(splitPane, "grow, span");
        } else {
            add(setupUI());
        }
        this.revalidate();
        this.repaint();
    }

    private JComboBox<Visibility.Level> createVisibilityDropdown(Visibility.Level level) {
        JComboBox<Visibility.Level> result = new JComboBox<>(Visibility.Level.values());
        result.setSelectedItem(level);
        result.addActionListener(a -> setVisibility((Visibility.Level) result.getSelectedItem()));
        result.setRenderer(DropdownRenderer.ofEnum(api, Visibility.Level.class, "misc.visibility_level"));
        return result;
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
        this.pluginNode = null;
        this.extendedConfig = null;
        this.lastSelection = null;
    }

    @Override
    public void afterLoadCompleteUI() {
        setEditingConfig(this.baseConfig);
        rebuildUI();
    }

    public void setCustomConfig(@Nullable ConfigSetting.Parent<?> config) {
        if (extendedConfig != null) this.extendedConfig.setAppended(config, pluginNode);
        setCorrectRoot();
    }

    public void updateConfigTreeListeners() {
        treeModel.updateListeners();

        TreePath selection = tabsTree == null ? null : tabsTree.getSelectionPath();
        tabsModel.updateListeners();
        if (selection != null) tabsTree.setSelectionPath(selection);
    }

    private void setSearch(String search) {
        treeModel.setSearch(search);
        tabsModel.setSearch(search);
        setCorrectRoot();
    }

    private void setVisibility(Visibility.Level level) {
        guiConfig.getValue().CONFIG_LEVEL = level;
        ConfigEntity.changed();

        treeModel.setVisibility(level);
        tabsModel.setVisibility(level);
        setCorrectRoot();
    }

    private void setCorrectRoot() {
        TreePath currentSelection = tabsTree == null ? null : tabsTree.getSelectionPath();

        treeModel.setRoot(packed ? baseConfig :
                treeModel.isSearching() ?
                        extendedConfig != null ? extendedConfig : baseConfig :
                        lastSelection != null ? lastSelection : baseConfig);
        tabsModel.setRoot(extendedConfig != null ? extendedConfig : baseConfig);

        if (currentSelection != null) tabsTree.setSelectionPath(currentSelection);
    }

    private JComponent setupUI() {
        EditorProvider renderer = api.requireInstance(EditorProvider.class);
        EditorProvider editor = new EditorProvider(renderer);

        configTree = new ConfigTree(treeModel, renderer, editor);

        JScrollPane scrollPane = wrapInScrollPane(configTree, false);

        if (packed) {
            Dimension treeSize = configTree.getPreferredSize();
            scrollPane.setPreferredSize(new Dimension(treeSize.width + 15, Math.min(400, treeSize.height)));
        }
        return new JLayer<>(scrollPane, new WheelScrollLayerUI());
    }

    private static JScrollPane wrapInScrollPane(JComponent component, boolean enforceWidth) {
        JScrollPane scrollPane = enforceWidth ? new WidthEnforcedScrollPane(component) : new JScrollPane(component);
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
            if (super.isLeaf(node)) return false;
            return ((ConfigSetting.Parent<?>) node).getChildren().values().stream().anyMatch(super::isLeaf);
        }
    }

}
