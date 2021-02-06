package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.extensions.plugins.PluginDefinition;

import java.util.HashSet;
import java.util.Set;

public class ConfigEntity {

    public static ConfigEntity INSTANCE = new ConfigEntity();
    private Config config;

    public NpcInfo getOrCreateNpcInfo(String name) {
        int mapId = MapManager.id;
        NpcInfo info = config.LOOT.NPC_INFOS.get(name);
        if (info == null) {
            info = new NpcInfo();

            info.radius = 500;
            info.mapList.add(mapId);

            if (!name.isEmpty()) {
                config.LOOT.NPC_INFOS.put(name, info);
                config.LOOT.MODIFIED_NPC.send(name);

                config.changed = true;
            }
        } else if (info.mapList.add(mapId)) {
            config.changed = true;
            config.LOOT.MODIFIED_NPC.send(name);
        }
        return info;
    }

    public BoxInfo getOrCreateBoxInfo(String name) {
        BoxInfo info = config.COLLECT.BOX_INFOS.get(name);
        if (info == null) {
            info = new BoxInfo();
            if (!name.isEmpty()) {
                config.COLLECT.BOX_INFOS.put(name, info);
                config.COLLECT.ADDED_BOX.send(name);

                config.changed = true;
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
                    config.changed = true;
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
        INSTANCE.config.changed = true;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

}
