package com.github.manolo8.darkbot.config.tree;

import com.github.manolo8.darkbot.config.tree.handlers.SettingHandlerFactory;
import eu.darkbot.api.API;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Option;
import eu.darkbot.api.extensions.PluginInfo;
import eu.darkbot.api.managers.I18nAPI;
import eu.darkbot.impl.config.ConfigSettingImpl;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class responsible for building a {@link ConfigSetting} tree from a class
 * This class does NOT fill in any details about the data present in the configuration,
 * only creates the containers for it.
 */
public class ConfigBuilder implements API.Singleton {

    private final I18nAPI i18n;
    private final SettingHandlerFactory settingHandlerFactory;

    public ConfigBuilder(I18nAPI i18n,
                         SettingHandlerFactory settingHandlerFactory) {
        this.i18n = i18n;
        this.settingHandlerFactory = settingHandlerFactory;
    }

    public <T> ConfigSetting.Parent<T> of(Class<T> type, String rootName, @Nullable PluginInfo namespace) {
        Configuration cfg = type.getAnnotation(Configuration.class);
        String baseKey = "config";
        boolean allOptions = false;

        if (cfg != null) {
            baseKey = cfg.value();
            allOptions = cfg.allOptions();
        } else {
            com.github.manolo8.darkbot.config.types.Option legacyOption =
                    type.getAnnotation(com.github.manolo8.darkbot.config.types.Option.class);
            if (legacyOption != null && !legacyOption.key().isEmpty())
                baseKey = legacyOption.key();
        }

        return new Builder(namespace, baseKey, allOptions).build(type, rootName);
    }

    private class Builder {
        private final PluginInfo namespace;
        private final String baseKey;
        private final boolean allConfig;

        public Builder(PluginInfo namespace,
                       String baseKey,
                       boolean allConfig) {
            this.namespace = namespace;
            this.baseKey = baseKey;
            this.allConfig = allConfig;
        }

        public <T> ConfigSettingImpl.Root<T> build(Class<T> type, String rootName) {
            return new ConfigSettingImpl.Root<T>(
                    namespace,
                    baseKey,
                    i18n.getOrDefault(namespace, baseKey, rootName),
                    i18n.getOrDefault(namespace, baseKey + ".desc", null),
                    type, settingHandlerFactory.getHandler(null, namespace),
                    parent -> getChildren(parent, type));
        }

        private Map<String, ConfigSetting<?>> getChildren(ConfigSetting.Parent<?> p, Class<?> type) {
            Configuration cfg = type.getAnnotation(Configuration.class);
            String parentKey = cfg != null ? cfg.value() : p.getKey();

            return Arrays.stream(type.getDeclaredFields())
                    .filter(this::participates)
                    .collect(Collectors.toMap(
                            f -> f.getName().toLowerCase(Locale.ROOT),
                            f -> createConfig(p, parentKey, f),
                            (a, b) -> a,
                            LinkedHashMap::new));
        }

        private ConfigSetting<?> createConfig(ConfigSetting.Parent<?> parent, String parentKey, Field field) {
            Class<?> type = field.getType();

            String key = parentKey + "." + field.getName().toLowerCase(Locale.ROOT);
            String name, description;

            com.github.manolo8.darkbot.config.types.Option legacyOption
                    = field.getAnnotation(com.github.manolo8.darkbot.config.types.Option.class);

            if (legacyOption != null) {
                if (!legacyOption.key().isEmpty()) key = legacyOption.key();

                name = i18n.getOrDefault(namespace, key, legacyOption.value());
                description = i18n.getOrDefault(namespace, key + ".desc",
                        legacyOption.description().isEmpty() ? null : legacyOption.description());
            } else {
                Option option = field.getAnnotation(Option.class);
                if (option != null && !option.value().isEmpty()) key = option.value();

                name = i18n.getOrDefault(namespace, key, "");
                description = i18n.getOrDefault(namespace, key + ".desc", null);
            }


            // If we know for sure it is a leaf, we ignore trying to make an intermediate
            // If the intermediate turns out not to have any children, discard it
            // and go back to it being a leaf node
            if (!isLeaf(field)) {
                ConfigSettingImpl.Intermediate<?> inter = new ConfigSettingImpl.Intermediate<>(parent,
                        key, name, description,
                        type, settingHandlerFactory.getHandler(field, namespace),
                        p -> getChildren(p, type));
                if (!inter.getChildren().isEmpty())
                    return inter;
            }

            return new ConfigSettingImpl.Leaf<>(parent, key, name, description, type,
                    settingHandlerFactory.getHandler(field, namespace));
        }

        /**
         * @param field the field to check
         * @return true if this field participates in the configuration tree, false otherwise
         */
        private boolean participates(Field field) {
            if (!Modifier.isPublic(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) return false;
            if (field.isAnnotationPresent(com.github.manolo8.darkbot.config.types.Option.class)) return true;
            if (field.isAnnotationPresent(Option.Ignore.class)) return false;
            if (field.isAnnotationPresent(Option.class)) return true;
            if (Modifier.isTransient(field.getModifiers())) return false;
            return allConfig;
        }

        /**
         * @param field the field to check
         * @return true if this is a leaf node, no more children under this, false otherwise
         */
        private boolean isLeaf(Field field) {
            Class<?> type = field.getType();
            return type.isPrimitive() || type.isInterface() || settingHandlerFactory.hasHandler(field);
        }

    }

}
