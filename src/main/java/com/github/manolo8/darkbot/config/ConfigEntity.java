package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.extensions.plugins.PluginDefinition;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.EventBrokerAPI;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigEntity {

    public static ConfigEntity INSTANCE;

    private final ConfigSetting<Config> root;
    private final ConfigSetting<Map<String, NpcInfo>> npcInfos;
    private final ConfigSetting<Map<String, BoxInfo>> boxInfos;
    private final EventBrokerAPI eventBroker;

    public ConfigEntity(ConfigAPI configAPI, EventBrokerAPI eventBroker) {
        this.root = configAPI.getConfigRoot();
        this.npcInfos = configAPI.requireConfig("loot.npc_infos");
        this.boxInfos = configAPI.requireConfig("collect.box_infos");
        this.eventBroker = eventBroker;
    }

    public NpcInfo getOrCreateNpcInfo(Npc npc) {
        String username = npc.getEntityInfo().getUsername();

        Map<String, NpcInfo> npcs = npcInfos.getValue();
        boolean unknown = npcs.get(username) == null;

        NpcInfo npcInfo = getOrCreateNpcInfo(username);
        if (unknown)
            eventBroker.sendEvent(new eu.darkbot.api.config.types.NpcInfo.NpcInfoCreateEvent(npc, npcInfo));
        return npcInfo;
    }

    public NpcInfo getOrCreateNpcInfo(String name) {
        int mapId = MapManager.id;

        Map<String, NpcInfo> npcs = npcInfos.getValue();
        NpcInfo info = npcs.get(name);
        if (info == null) {
            info = new NpcInfo();

            info.name = name;
            info.radius = 560;
            info.mapList.add(mapId);

            if (!name.isEmpty()) {
                npcs.put(name, info);

                npcInfos.setValue(npcs);
                changed();
            }
        } else if (info.mapList.add(mapId)) {
            npcInfos.setValue(npcs);
            changed();
        }
        return info;
    }

    public BoxInfo getOrCreateBoxInfo(String name) {
        Map<String, BoxInfo> boxes = boxInfos.getValue();
        BoxInfo info = boxes.get(name);
        if (info == null) {
            info = new BoxInfo();
            info.name = name;

            if (!name.isEmpty()) {
                boxes.put(name, info);

                boxInfos.setValue(boxes);
                changed();
            }
        }
        return info;
    }

    public void updateSafetyFor(Entity entity) {
        if (!entity.locationInfo.isLoaded() ||
                (entity.locationInfo.now.x == 0 && entity.locationInfo.now.y == 0)) return;
        SafetyInfo.Type type = SafetyInfo.Type.of(entity);
        if (type == null) return;

        Set<SafetyInfo> safetyInfos = getOrCreateSafeties();

        getConfig().ADDED_SAFETY.send(safetyInfos.stream().filter(info -> info.type == type
                && info.x == (int) entity.locationInfo.now.x
                && info.y == (int) entity.locationInfo.now.y)
                .peek(info -> info.entity = entity)
                .findFirst()
                .orElseGet(() -> {
                    SafetyInfo s = new SafetyInfo(type, (int) entity.locationInfo.now.x, (int) entity.locationInfo.now.y, entity);
                    safetyInfos.add(s);
                    changed();
                    return s;
                }));
    }

    public ZoneInfo getOrCreatePreferred() {
        if (MapManager.id < 0) return new ZoneInfo(1);
        return getConfig().PREFERRED.computeIfAbsent(MapManager.id, id -> new ZoneInfo(getConfig().BOT_SETTINGS.OTHER.ZONE_RESOLUTION));
    }

    public ZoneInfo getOrCreateAvoided() {
        if (MapManager.id < 0) return new ZoneInfo(1);
        return getConfig().AVOIDED.computeIfAbsent(MapManager.id, id -> new ZoneInfo(getConfig().BOT_SETTINGS.OTHER.ZONE_RESOLUTION));
    }

    public Set<SafetyInfo> getOrCreateSafeties() {
        return getConfig().SAFETY.computeIfAbsent(MapManager.id, id -> new HashSet<>());
    }

    public PluginConfig getPluginInfo(PluginDefinition plugin) {
        return getConfig().PLUGIN_INFOS.computeIfAbsent(plugin.name + "_by_" + plugin.author, id -> new PluginConfig());
    }

    public static void changed() {
        INSTANCE.getConfig().changedAt = System.currentTimeMillis();
        INSTANCE.getConfig().changed = true;
    }

    public Config getConfig() {
        return root.getValue();
    }

    @Deprecated
    public void setConfig(Config config) {
    }

}
