package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.extensions.plugins.PluginDefinition;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.managers.ConfigAPI;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigEntity {

    public static ConfigEntity INSTANCE;

    static {
        INSTANCE = HeroManager.instance.main.pluginAPI.requireInstance(ConfigEntity.class);
    }

    private final ConfigAPI configAPI;
    private final ConfigSetting<Map<String, NpcInfo>> npcInfos;
    private final ConfigSetting<Map<String, BoxInfo>> boxInfos;

    public ConfigEntity(ConfigAPI configAPI) {
        this.configAPI = configAPI;
        this.npcInfos = configAPI.requireConfig("loot.npc_infos");
        this.boxInfos = configAPI.requireConfig("collect.box_infos");
    }

    private Config config;

    public NpcInfo getOrCreateNpcInfo(String name) {
        int mapId = MapManager.id;

        Map<String, NpcInfo> npcs = npcInfos.getValue();
        NpcInfo info = npcs.get(name);
        if (info == null) {
            info = new NpcInfo();

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

        config.ADDED_SAFETY.send(safetyInfos.stream().filter(info -> info.type == type
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
        return config.PREFERRED.computeIfAbsent(MapManager.id, id -> new ZoneInfo(config.BOT_SETTINGS.OTHER.ZONE_RESOLUTION));
    }

    public ZoneInfo getOrCreateAvoided() {
        if (MapManager.id < 0) return new ZoneInfo(1);
        return config.AVOIDED.computeIfAbsent(MapManager.id, id -> new ZoneInfo(config.BOT_SETTINGS.OTHER.ZONE_RESOLUTION));
    }

    public Set<SafetyInfo> getOrCreateSafeties() {
        return config.SAFETY.computeIfAbsent(MapManager.id, id -> new HashSet<>());
    }

    public PluginConfig getPluginInfo(PluginDefinition plugin) {
        return config.PLUGIN_INFOS.computeIfAbsent(plugin.name + "_by_" + plugin.author, id -> new PluginConfig());
    }

    public static void changed() {
        INSTANCE.config.changedAt = System.currentTimeMillis();
        INSTANCE.config.changed = true;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

}
