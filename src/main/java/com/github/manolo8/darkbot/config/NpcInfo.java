package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.core.itf.NpcExtraProvider;
import com.github.manolo8.darkbot.extensions.features.Feature;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Option(value = "Name", description = "Npc names, npcs ending in * are grouped")
public class NpcInfo {

    @Option(value = "Radius", description = "Distance to keep from this npc")
    public double radius;
    @Option(value = "Priority", description = "#1 priority will be targeted before #2 priority")
    public int priority;
    @Option(value = "Kill", description = "If this npc should be killed")
    public boolean kill;
    @Option(value = "Ammo key", description = "Special ammo for this npc, if unset, default is used")
    public Character attackKey;

    public static transient Map<String, NpcExtraFlag> NPC_FLAGS = new LinkedHashMap<>();

    @Option(value = "Extra", description = "Several extra flags for the npc")
    public ExtraNpcInfo extra = new ExtraNpcInfo();

    public static class ExtraNpcInfo {
        // TODO: remove in a few versions.
        public boolean noCircle;
        public boolean ignoreOwnership;
        public boolean ignoreAttacked;
        public boolean attackSecond;
        public boolean passive;
        private boolean updated;

        private Set<String> flags = new HashSet<>();

        public ExtraNpcInfo() {}

        public ExtraNpcInfo(NpcExtraFlag... flags) {
            this.updated = true;
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

        private void update() {
            if (!updated) {
                updated = true;
                set(NpcExtra.NO_CIRCLE, noCircle);
                set(NpcExtra.IGNORE_OWNERSHIP, ignoreOwnership);
                set(NpcExtra.IGNORE_ATTACKED, ignoreAttacked);
                set(NpcExtra.PASSIVE, passive);
                set(NpcExtra.ATTACK_SECOND, attackSecond);
            }
        }

        public String toString() {
            update();
            return flags.stream().map(NPC_FLAGS::get)
                    .filter(Objects::nonNull)
                    .map(NpcExtraFlag::getShortName)
                    .collect(Collectors.joining(","));
        }
    }

    public Set<Integer> mapList = new HashSet<>();

    public void set(Double radius, Integer priority, Boolean kill, Character attackKey, ExtraNpcInfo extra) {
        if (radius != null) this.radius = radius;
        if (priority != null) this.priority = priority;
        if (kill != null) this.kill = kill;
        if (attackKey != null) this.attackKey = attackKey;
        if (extra != null) this.extra = extra;
    }

    public void copyOf(NpcInfo other) {
        this.radius = other.radius;
        this.priority = other.priority;
        this.kill = other.kill;
        this.attackKey = other.attackKey;
        other.extra.update();
        this.extra.flags = new HashSet<>(other.extra.flags);
    }
}
