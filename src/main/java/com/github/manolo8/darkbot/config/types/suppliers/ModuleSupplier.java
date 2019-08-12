package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.extensions.modules.CustomModule;

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

public class ModuleSupplier extends OptionList<String> {
    private static Set<ModuleSupplier> INSTANCES = Collections.newSetFromMap(new WeakHashMap<>());
    private static Map<String, CustomModule> MODULES_BY_ID;
    private static List<String> MODULE_NAMES;

    public static void updateModules(Map<String, CustomModule> MODULES_BY_ID) {
        ModuleSupplier.MODULES_BY_ID = MODULES_BY_ID;
        MODULE_NAMES = MODULES_BY_ID.values().stream().map(CustomModule::name).collect(Collectors.toList());
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
        return MODULES_BY_ID.get(id).description();
    }

    @Override
    public String getText(String id) {
        return MODULES_BY_ID.get(id).name();
    }

    @Override
    public List<String> getOptions() {
        return MODULE_NAMES;
    }

}
