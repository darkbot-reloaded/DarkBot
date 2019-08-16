package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.extensions.features.Feature;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class ModuleSupplier extends OptionList<String> {
    private static Set<ModuleSupplier> INSTANCES = Collections.newSetFromMap(new WeakHashMap<>());
    private static Map<String, Feature> MODULES_BY_ID;
    private static List<String> MODULE_NAMES;

    public static void updateModules(Map<String, Feature> FEATURES) {
        ModuleSupplier.MODULES_BY_ID = FEATURES;
        MODULE_NAMES = MODULES_BY_ID.values().stream().map(Feature::name).collect(Collectors.toList());
        INSTANCES.forEach(model -> {
            ListDataEvent ev = new ListDataEvent(model, ListDataEvent.CONTENTS_CHANGED, 0, MODULES_BY_ID.size());
            Arrays.stream(model.dataListeners.getListeners(ListDataListener.class))
                    .forEach(listener -> listener.contentsChanged(ev));
        });
    }

    public ModuleSupplier() {
        INSTANCES.add(this);
    }

    @Override
    public String getValue(String name) {
        //noinspection StringEquality
        return MODULES_BY_ID.entrySet()
                .stream()
                .filter(e -> e.getValue().name() == name)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getTooltipFromVal(String id) {
        Feature feature = MODULES_BY_ID.get(id);
        return feature == null ? null : feature.description();
    }

    @Override
    public String getText(String id) {
        Feature feature = MODULES_BY_ID.get(id);
        return feature == null ? null : feature.name();
    }

    @Override
    public List<String> getOptions() {
        return MODULE_NAMES;
    }

}
