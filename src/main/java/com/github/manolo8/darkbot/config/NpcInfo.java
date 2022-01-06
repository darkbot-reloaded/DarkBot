package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.core.itf.NpcExtraProvider;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.objects.facades.SettingsProxy;
import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Option;
import eu.darkbot.api.game.items.ItemCategory;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.managers.HeroItemsAPI;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration("config.loot.npc_table")
public class NpcInfo implements eu.darkbot.api.config.types.NpcInfo {

    private final HeroItemsAPI items;

    public double radius;
    public int priority;
    public boolean kill;
    @Option("config.loot.npc_table.attack_key")
    public Character attackKey;
    @Option("config.loot.npc_table.attack_formation")
    public Character attackFormation;
    public ExtraNpcInfo extra = new ExtraNpcInfo();

    public transient int npcId;
    public @Option.Ignore Set<Integer> mapList = new HashSet<>();

    public static transient final Map<String, NpcExtraFlag> NPC_FLAGS = new LinkedHashMap<>();

    public NpcInfo() { //should be reworked when ConfigEntity will rework
        this.items = HeroManager.instance.main.pluginAPI.requireAPI(HeroItemsAPI.class);
    }

    public static void setNpcFlags(Stream<NpcExtraProvider> flags) {
        NPC_FLAGS.clear();
        flags.map(NpcExtraProvider::values)
                .flatMap(Arrays::stream)
                .forEach(flag -> NPC_FLAGS.put(flag.getId(), flag));
    }

    public void set(Double radius, Integer priority, Boolean kill, Character attackKey, ExtraNpcInfo extra) {
        set(radius, priority, kill, attackKey, null, extra);
    }

    public void set(Double radius, Integer priority, Boolean kill, Character attackKey, Character attackFormation, ExtraNpcInfo extra) {
        if (radius != null) this.radius = radius;
        if (priority != null) this.priority = priority;
        if (kill != null) this.kill = kill;
        if (attackKey != null) this.attackKey = attackKey;
        if (attackFormation != null) this.attackFormation = attackFormation;
        if (extra != null) this.extra = extra;
    }

    public void copyOf(NpcInfo other) {
        this.radius = other.radius;
        this.priority = other.priority;
        this.kill = other.kill;
        this.attackKey = other.attackKey;
        this.attackFormation = other.attackFormation;
        this.extra.flags = new HashSet<>(other.extra.flags);
    }

    @Override
    public boolean shouldKill() {
        return kill;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public Optional<SelectableItem.Laser> getAmmo() {
        return findItemAssociatedWith(ItemCategory.LASERS, attackKey, SelectableItem.Laser.class);
    }

    @Override
    public Optional<SelectableItem.Formation> getFormation() {
        return findItemAssociatedWith(ItemCategory.DRONE_FORMATIONS, attackFormation, SelectableItem.Formation.class);
    }

    private <T extends Enum<T> & SelectableItem> Optional<T> findItemAssociatedWith(ItemCategory category, Character c, Class<T> type) {
        if (c == null) return Optional.empty();

        return Optional.ofNullable(items.getItem(c, category))
                .map(i -> i.getAs(type));
    }

    @Override
    public boolean hasExtraFlag(Enum<?> flag) {
        return extra.has(getId(flag));
    }

    @Override
    public void setExtraFlag(Enum<?> flag, boolean active) {
        extra.set(getId(flag), active);
    }

    private String getId(Enum<?> flag) {
        return flag.getClass().getCanonicalName() + flag.name();
    }

    public static class ExtraNpcInfo {
        private Set<String> flags = new HashSet<>();

        public ExtraNpcInfo() {}

        public ExtraNpcInfo(NpcExtraFlag... flags) {
            for (NpcExtraFlag flag : flags) set(flag, true);
        }

        public boolean has(NpcExtraFlag flag) {
            return has(flag.getId());
        }

        public boolean has(String flagId) {
            return flags.contains(flagId);
        }

        public void set(NpcExtraFlag flag, boolean set) {
            set(flag.getId(), set);
        }

        public void set(String flagId, boolean set) {
            if (set) flags.add(flagId);
            else flags.remove(flagId);
        }

        public void copy(ExtraNpcInfo other) {
            if (this == other) return;
            this.flags.clear();
            this.flags.addAll(other.flags);
        }

        public String toString() {
            if (flags.isEmpty()) return "";

            return NPC_FLAGS.entrySet().stream()
                    .filter(e -> flags.contains(e.getKey()))
                    .map(Map.Entry::getValue)
                    .map(NpcExtraFlag::getShortName)
                    .collect(Collectors.joining(","));
        }
    }

}
