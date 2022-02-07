package com.github.manolo8.darkbot.gui.utils.tree;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.PluginInfo;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A config node that holds a list of plugins
 *
 * It needs to perform some re-parenting tricks so that searches on the tree by parent nodes
 * can properly filter down.
 *
 * If we did not do this, when searching "Plugin Name" the plugin itself would show up but none of the sub-settings
 * for individual features on the plugin would show up, because their getParent() would be null, and they
 * couldn't see that a parent node matches the filter.
 */
public class PluginListConfigSetting extends DummyConfigSetting<Void> {

    private final Map<String, ConfigSetting<?>> children = new LinkedHashMap<>();

    public PluginListConfigSetting(ConfigSetting.Parent<?> parent, FeatureRegistry featureRegistry) {
        super("Plugins", parent);

        for (PluginInfo plugin : featureRegistry.getPluginInfos())
            children.put(plugin.getName().toLowerCase(Locale.ROOT),
                    new PluginConfigSetting(plugin, this, featureRegistry));
    }

    @Override
    public Map<String, ConfigSetting<?>> getChildren() {
        return children;
    }

    /**
     * A config node that holds a plugin and all of its features
     */
    private static class PluginConfigSetting extends DummyConfigSetting<Void> implements ToggleableNode {

        private final Map<String, ConfigSetting<?>> children = new LinkedHashMap<>();

        public PluginConfigSetting(PluginInfo plugin, ConfigSetting.Parent<?> parent, FeatureRegistry extensions) {
            super(plugin.getName(), parent);

            for (String featureId : ((Plugin) plugin).getFeatureIds()) {
                FeatureDefinition<?> feature = extensions.getFeatureDefinition(featureId);
                if (feature != null && feature.getConfig() != null)
                    children.put(feature.getId(), new FeatureSettingParent<>(feature.getConfig(), this, feature));
            }
        }

        @Override
        public Map<String, ConfigSetting<?>> getChildren() {
            return children;
        }

        @Override
        public boolean isShown() {
            return children.values().stream().anyMatch(s -> ((ToggleableNode) s).isShown());
        }
    }

    private static class FeatureSettingParent<T> extends ReParentingSettingParent<T> implements ToggleableNode {

        private final FeatureDefinition<?> fd;

        public FeatureSettingParent(ConfigSetting.Parent<T> setting, ConfigSetting.Parent<?> parent,
                                    FeatureDefinition<?> fd) {
            super(setting, parent);
            this.fd = fd;
        }

        public boolean isShown() {
            return fd.isEnabled();
        }

    }

    private static <T> ConfigSetting<T> reParent(ConfigSetting<T> setting, ConfigSetting.Parent<?> parent) {
        if (setting instanceof ConfigSetting.Parent)
            return new ReParentingSettingParent<>((ConfigSetting.Parent<T>) setting, parent);
        else return new ReParentingSetting<>(setting, parent);
    }

    /**
     * A config node that reparents a setting to a new parent
     */
    private static class ReParentingSetting<T> extends ForwardingConfigSetting<T> {
        private final ConfigSetting<T> setting;
        private final ConfigSetting.Parent<?> parent;

        public ReParentingSetting(ConfigSetting<T> setting, Parent<?> parent) {
            this.setting = setting;
            this.parent = parent;
        }

        @Override
        public ConfigSetting<T> delegate() {
            return setting;
        }

        @Override
        public @Nullable Parent<?> getParent() {
            return parent;
        }

    }

    /**
     * A config node that reparents a setting to a new parent, recursively for children
     */
    private static class ReParentingSettingParent<T> extends ReParentingSetting<T> implements ConfigSetting.Parent<T> {

        private final Map<String, ConfigSetting<?>> children = new LinkedHashMap<>();

        public ReParentingSettingParent(ConfigSetting.Parent<T> setting, ConfigSetting.Parent<?> parent) {
            super(setting, parent);
            setting.getChildren().forEach((k, v) -> children.put(k, reParent(v, this)));
        }

        @Override
        public Map<String, ConfigSetting<?>> getChildren() {
            return children;
        }
    }

}
