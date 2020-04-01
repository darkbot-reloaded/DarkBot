package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.types.Option;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Option(key = "config.loot.npc_table.name")
public class NpcInfo {

    @Option(key = "config.loot.npc_table.radius")
    public double radius;
    @Option(key = "config.loot.npc_table.priority")
    public int priority;
    @Option(key = "config.loot.npc_table.kill")
    public boolean kill;
    @Option(key = "config.loot.npc_table.attack_key")
    public Character attackKey;
    @Option(key = "config.loot.npc_table.attack_formation")
    public Character attackFormation;

    public int npcId;

    public static transient Map<String, NpcExtraFlag> NPC_FLAGS = new LinkedHashMap<>();

    @Option(key = "config.loot.npc_table.extra")
    public ExtraNpcInfo extra = new ExtraNpcInfo();

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

        public String toString() {
            return flags.stream().map(NPC_FLAGS::get)
                    .filter(Objects::nonNull)
                    .map(NpcExtraFlag::getShortName)
                    .collect(Collectors.joining(","));
        }
    }

    public Set<Integer> mapList = new HashSet<>();

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
}
