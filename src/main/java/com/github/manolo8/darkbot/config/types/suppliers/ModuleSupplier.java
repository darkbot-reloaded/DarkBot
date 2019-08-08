package com.github.manolo8.darkbot.config.types.suppliers;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class ModuleSupplier extends OptionList<String> {
    private static Set<ModuleSupplier> INSTANCES = Collections.newSetFromMap(new WeakHashMap<>());
    private static Map<String, String> MODULE_NAMES_BY_ID;
    private static Map<String, String> MODULE_TOOLTIPS_BY_ID;
    private static List<String> MODULE_NAMES;

    public static void updateModules(Map<String, String> MODULE_NAMES_BY_ID,
                                     Map<String, String> MODULE_TOOLTIPS_BY_ID) {
        ModuleSupplier.MODULE_NAMES_BY_ID = MODULE_NAMES_BY_ID;
        ModuleSupplier.MODULE_TOOLTIPS_BY_ID = MODULE_TOOLTIPS_BY_ID;
        MODULE_NAMES = new ArrayList<>(MODULE_NAMES_BY_ID.values());
        INSTANCES.forEach(model -> {
            ListDataEvent ev = new ListDataEvent(model, ListDataEvent.CONTENTS_CHANGED, 0, MODULE_NAMES_BY_ID.size());
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
        return MODULE_NAMES_BY_ID.entrySet()
                .stream()
                .filter(e -> e.getValue() == name)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getTooltipFromVal(String id) {
        return MODULE_TOOLTIPS_BY_ID.get(id);
    }

    @Override
    public String getText(String id) {
        return MODULE_NAMES_BY_ID.get(id);
    }

    @Override
    public List<String> getOptions() {
        return MODULE_NAMES;
    }

}
