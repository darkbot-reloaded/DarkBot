package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.modules.TemporalModule;
import eu.darkbot.api.config.annotations.Dropdown;
import eu.darkbot.api.managers.ExtensionsAPI;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class ModuleSupplier implements Dropdown.Options<String> {
    private static Map<String, FeatureDefinition<?>> MODULES_BY_ID;
    private static List<String> MODULE_IDS = new ArrayList<>();

    public static void updateModules(Map<String, FeatureDefinition<?>> modules) {
        MODULES_BY_ID = modules;
        MODULE_IDS = MODULES_BY_ID.values().stream()
                .filter(m -> !TemporalModule.class.isAssignableFrom(m.getClazz()))
                .map(FeatureDefinition::getId).collect(Collectors.toList());
    }

    @Override
    public List<String> options() {
        return MODULE_IDS;
    }

    @Override
    public String getText(String id) {
        if (id == null) return "";
        FeatureDefinition<?> feature = MODULES_BY_ID.get(id);
        return feature == null ? "" : feature.getName();
    }

    @Override
    public String getTooltip(String id) {
        FeatureDefinition<?> feature = MODULES_BY_ID.get(id);
        return feature == null ? null : feature.getDescription();
    }

}
