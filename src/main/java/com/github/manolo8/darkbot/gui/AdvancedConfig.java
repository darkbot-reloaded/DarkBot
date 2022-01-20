package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.config.tree.ConfigSettingTree;
import com.github.manolo8.darkbot.extensions.plugins.PluginListener;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.tree.ConfigTree;
import com.github.manolo8.darkbot.gui.tree.EditorProvider;
import com.github.manolo8.darkbot.gui.tree.TreeEditor;
import com.github.manolo8.darkbot.gui.tree.TreeRenderer;
import com.github.manolo8.darkbot.gui.utils.SearchField;
import com.github.manolo8.darkbot.gui.utils.SimpleTreeListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.ValueHandler;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.LayerUI;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class AdvancedConfig extends JPanel implements PluginListener {

    public static final int EDITOR_HEIGHT = 17;
    public static final int ROW_HEIGHT = 19;
    public static final int HEADER_HEIGHT = 26;

    private final PluginAPI api;

    private ConfigSetting.Parent<?> baseConfig, extendedConfig, lastSelection;
    private final ConfigSettingTree treeModel = new ConfigSettingTree();

    private JPanel tabs;
    private final Map<String, TabButton> buttons = new LinkedHashMap<>();
    private boolean packed = false; // If this is a packed config in a floating window

    public AdvancedConfig(PluginAPI api) {
        super(new MigLayout("ins 0, gap 0, fill, wrap 2", "[][grow]", "[][grow]"));
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
        this.extendedConfig = config;
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
        setCorrectRoot();
        if (!packed) {
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
        this.extendedConfig = null;
        this.lastSelection = null;
    }

    @Override
    public void afterLoadCompleteUI() {
        setEditingConfig(this.baseConfig);
        rebuildUI();
    }

    public void setCustomConfig(ConfigSetting.Parent<?>... configs) {
        this.extendedConfig = new CompoundConfigSetting<>(this.baseConfig, configs);
        setCorrectRoot();
        buttons.clear();
        updateTabs();
    }

    private void setSearch(String search) {
        boolean wasFiltered = treeModel.isFiltered();
        treeModel.setSearch(search);

        if (wasFiltered != treeModel.isFiltered()) {
            setCorrectRoot();
            updateTabs();
        } else {
            treeModel.updateListeners();
        }
    }

    private void setCorrectRoot() {
        treeModel.setRoot(packed ? baseConfig :
                treeModel.isFiltered() ?
                        extendedConfig != null ? extendedConfig : baseConfig :
                        lastSelection != null ? lastSelection : baseConfig);
    }

    private void updateTabs() {
        if (tabs == null) return;
        tabs.removeAll();
        if (!treeModel.isFiltered()) {
            for (Map.Entry<String, ConfigSetting<?>> entry : extendedConfig.getChildren().entrySet()) {
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
            treeModel.setRoot(lastSelection = node);
            buttons.values().forEach(TabButton::update);
            treeModel.updateListeners();
        }
    }

    private JComponent setupUI() {
        EditorProvider renderer = api.requireInstance(EditorProvider.class);
        EditorProvider editor = new EditorProvider(renderer);

        ConfigTree configTree = new ConfigTree(treeModel, renderer, editor);

        JScrollPane scrollPane = new JScrollPane(configTree);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(25);

        if (packed) {
            Dimension treeSize = configTree.getPreferredSize();
            scrollPane.setPreferredSize(new Dimension(treeSize.width + 15, Math.min(400, treeSize.height)));
        }
        return new JLayer<>(scrollPane, new WheelScrollLayerUI());
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

    private static class CompoundConfigSetting<T> implements ConfigSetting.Parent<T> {

        private final ConfigSetting.Parent<T> base;
        private final Map<String, ConfigSetting<?>> remapped;

        public CompoundConfigSetting(ConfigSetting.Parent<T> base,
                                     ConfigSetting<?>... appended) {
            this.base = base;
            this.remapped = new LinkedHashMap<>();
            remapped.putAll(base.getChildren());

            if (appended == null) return;
            for (ConfigSetting<?> child : appended) {
                if (child == null) continue;
                String baseKey = child.getKey();
                // If key isn't configured for this root, generate one from name
                if (baseKey.isEmpty() || baseKey.equals("config"))
                    baseKey = child.getName().toLowerCase(Locale.ROOT).replace(" ", "_");
                // If the key isn't unique, append _ at the end until it is
                while (remapped.containsKey(baseKey)) baseKey += "_";
                remapped.put(baseKey, child);
            }
        }

        @Override
        public Map<String, ConfigSetting<?>> getChildren() {
            return remapped;
        }

        @Override
        public @Nullable Parent<?> getParent() {
            return base.getParent();
        }

        @Override
        public String getKey() {
            return base.getKey();
        }

        @Override
        public String getName() {
            return base.getName();
        }

        @Override
        public @Nullable String getDescription() {
            return base.getDescription();
        }

        @Override
        public Class<T> getType() {
            return base.getType();
        }

        @Override
        public T getValue() {
            return base.getValue();
        }

        @Override
        public void setValue(T t) {
            base.setValue(t);
        }

        @Override
        public void addListener(Consumer<T> consumer) {
            base.addListener(consumer);
        }

        @Override
        public void removeListener(Consumer<T> consumer) {
            base.removeListener(consumer);
        }

        @Override
        public ValueHandler<T> getHandler() {
            return base.getHandler();
        }

    }

}
