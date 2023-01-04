package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import eu.darkbot.api.config.annotations.Dropdown;
import eu.darkbot.api.extensions.TemporalModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public @NotNull String getText(@Nullable String id) {
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
