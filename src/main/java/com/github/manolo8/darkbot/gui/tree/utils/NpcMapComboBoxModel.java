package com.github.manolo8.darkbot.gui.tree.utils;

import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.gui.utils.Strings;
import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.managers.StarSystemAPI;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NpcMapComboBoxModel extends AbstractListModel<String>
        implements ComboBoxModel<String>, Consumer<Map<String, NpcInfo>> {

    private static final String DEFAULT_ALL = "*";
    private final StarSystemAPI starSystemAPI;
    private final Consumer<Set<Integer>> onSelect;

    private List<String> maps;
    private String selected;
    private final Set<Integer> selectedMaps = new HashSet<>();

    public NpcMapComboBoxModel(StarSystemAPI starSystemAPI, Consumer<Set<Integer>> onSelect) {
        this.starSystemAPI = starSystemAPI;
        this.onSelect = onSelect;
    }

    @Override
    public void accept(Map<String, NpcInfo> infos) {
        Set<Integer> mapIds = infos.values().stream()
                .flatMap(n -> n.mapList.stream()).collect(Collectors.toSet());

        maps = Stream.concat(Stream.of(DEFAULT_ALL), starSystemAPI.getMaps().stream()
                .filter(map -> map.getId() >= 0 && mapIds.contains(map.getId()))
                .map(GameMap::getName).map(Strings::simplifyName)
                .distinct()).collect(Collectors.toList());

        if ((selected == null || !maps.contains(selected)) && !maps.isEmpty()) {
            setSelectedItem(maps.get(0));
        }
        fireContentsChanged(this, 0, maps.size() - 1);
    }

    @Override
    public void setSelectedItem(Object selected) {
        if (Objects.equals(this.selected, selected)) return;
        this.selected = (String) selected;

        selectedMaps.clear();
        if (selected != null && !selected.equals(DEFAULT_ALL))
            starSystemAPI.getMaps().stream()
                    .filter(m -> m.getId() >= 0 && selected.equals(Strings.simplifyName(m.getName())))
                    .mapToInt(GameMap::getId)
                    .forEach(selectedMaps::add);
        if (onSelect != null)
            onSelect.accept(selectedMaps);

        fireContentsChanged(this, -1, -1);
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }

    public Set<Integer> getSelectedMaps() {
        return selectedMaps;
    }

    @Override
    public int getSize() {
        return maps.size();
    }

    @Override
    public String getElementAt(int index) {
        return maps.get(index);
    }
}
