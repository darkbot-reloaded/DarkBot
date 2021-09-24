package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.gui.utils.Strings;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

@Deprecated
public class NpcNameSupplier extends OptionList<String> {

    private final static Set<NpcNameSupplier> INSTANCES = Collections.newSetFromMap(new WeakHashMap<>());
    private static List<String> NPC_LIST;
    static {
        updateNpcList();
        ConfigEntity.INSTANCE.getConfig().LOOT.MODIFIED_NPC.add(npc -> updateNpcList());
    }

    private static void updateNpcList() {
        NPC_LIST = ConfigEntity.INSTANCE.getConfig().LOOT.NPC_INFOS.keySet()
                .stream()
                .map(Strings::simplifyName)
                .distinct()
                .sorted(Comparator.comparing(name -> name.replaceAll("[^A-Za-z]", "")))
                .collect(Collectors.toList());
        INSTANCES.forEach(model -> {
            ListDataEvent ev = new ListDataEvent(model, ListDataEvent.CONTENTS_CHANGED, 0, NPC_LIST.size());
            Arrays.stream(model.dataListeners.getListeners(ListDataListener.class))
                    .forEach(listener -> listener.contentsChanged(ev));
        });
    }

    @Override
    public String getValue(String text) {
        return text;
    }

    @Override
    public String getText(String value) {
        return value;
    }

    @Override
    public List<String> getOptions() {
        return NPC_LIST;
    }

}
